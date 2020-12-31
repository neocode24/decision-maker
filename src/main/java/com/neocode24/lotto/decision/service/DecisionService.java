package com.neocode24.lotto.decision.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neocode24.lotto.decision.domain.LottoDecisionDomain;
import com.neocode24.lotto.decision.domain.LottoDomain;
import com.neocode24.lotto.decision.service.DecisionDbService;
import com.neocode24.lotto.decision.service.LottoUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
public class DecisionService {

    /**
     * Lotto 서버 주소
     */
    @Value("${spring.lotto.server.http-uri:https://m.dhlottery.co.kr}")
    private String lottoServerUri;

    /**
     * DB 제어 Mapper
     */
    private DecisionDbService decisionDbService;

    /**
     * RestTemplate
     */
    private RestTemplate restTemplate;


    private static final String QRCODE_URI_PREFIX = "http://m.dhlottery.co.kr/?v=";

    @Autowired
    public DecisionService(RestTemplateBuilder restTemplateBuilder, DecisionDbService decisionDbService) {

        this.decisionDbService = decisionDbService;

        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(120))
                .build();

    }

    /**
     * QR Code URL을 입력값으로 로또 사이트에서 HTML Body 얻기
     * @param qrCodeUri
     * @return
     * @throws Exception
     */
    public LottoDecisionDomain getLottoHtmlDataFromServer(String qrCodeUri, String clientIp) throws Exception {

        if ( qrCodeUri.indexOf(QRCODE_URI_PREFIX) < 0 ) {
            throw new Exception("QR Code 정보가 없거나, Lotto 형식이 아닙니다.");
        }

        // QR Code 의 "v" value 부분 획득
        String value = qrCodeUri.substring(qrCodeUri.indexOf(QRCODE_URI_PREFIX) + QRCODE_URI_PREFIX.length());

        // 구매 내역 호출
        String responseBody = callService(lottoServerUri + "/qr.do?method=winQr&v=" + value, createHeader(), HttpMethod.GET);

        // LottoDomain 정보 모음 변환하여 획득.
        List<LottoDomain> lottoDomains = getLottoNumbersFromHtmlBody(responseBody);

        // 중복 제외된 번호
        Set<Integer> deduplicatedNumbers = LottoUtils.getDeduplicatedNumbers(lottoDomains);


        // 새로운 로또 번호 5개 생성.
        List<LottoDomain> newLottoDomains = new ArrayList<>();
        for ( int i = 0 ; i < 5 ; i++ ) {
            newLottoDomains.add(
                    LottoUtils.getLottoDomain(deduplicatedNumbers)
            );
        }

        LottoDecisionDomain lottoDecisionDomain =
                LottoDecisionDomain.builder()
                        .clientIp(clientIp)
                        .createDateTime(LocalDateTime.now())
                        .deduplicatedNumbers(deduplicatedNumbers)
                        .lottoDomains(newLottoDomains)
                        .build();

        decisionDbService.insert(lottoDecisionDomain);

        return lottoDecisionDomain;
    }

    /**
     * Lotto Html Body 에서 LottoDomain 객체로 생성. 구매 내역에 따라 최대 5개까지 List에 담길수 있음.
     * @param htmlBody
     * @return
     */
    private List<LottoDomain> getLottoNumbersFromHtmlBody(String htmlBody) {

        List<LottoDomain> lottoDomains = new ArrayList<>();


        // HTML Body 중에 번호가 있는 <tbody> </tbody> 부분만 취득
        String tbodyStr = null;
        try {
            tbodyStr = htmlBody.substring(htmlBody.indexOf("<tbody>"), htmlBody.indexOf("</tbody>") + "</tbody>".length());
        } catch (StringIndexOutOfBoundsException e) {
            log.error("정상적인 Lotto 데이터가 아닙니다.");
            throw e;
        }


        // XML로 변경. JavaScript 부분은 XML로 인식이 안되서 실제 HTML 코드 중에 일부분만 XML로 발췌해서 변환
        Document document = getXmlDocument(tbodyStr);

        // <tr> 부분 순회 해서 숫자 인식
        NodeList trs = document.getElementsByTagName("tr");
        for ( int i = 0 ; i < trs.getLength() ; i++ ) {
            Element parameter = (Element) trs.item(i);
            Integer number1 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(0)).getTextContent().trim() );
            Integer number2 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(1)).getTextContent().trim() );
            Integer number3 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(2)).getTextContent().trim() );
            Integer number4 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(3)).getTextContent().trim() );
            Integer number5 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(4)).getTextContent().trim() );
            Integer number6 = Integer.parseInt( ((Element) parameter.getElementsByTagName("span").item(5)).getTextContent().trim() );

            // LottoDomain 객체로 생성
            lottoDomains.add(
                LottoDomain.builder()
                        .number1(number1)
                        .number2(number2)
                        .number3(number3)
                        .number4(number4)
                        .number5(number5)
                        .number6(number6)
                        .build()
            );
        }

        return lottoDomains;
    }

    /**
     * Html -> XML 변환
     * @param htmlBody
     * @return
     */
    private Document getXmlDocument(String htmlBody) {

        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(htmlBody));

            return builder.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("HTML이 잘못된 형식입니다.");
        }

        return null;
    }


    /**
     * 로또 사이트 호출 공통화
     * @param uri
     * @param header
     * @param httpMethod
     * @param <T>
     * @return
     * @throws URISyntaxException
     */
    private <T> T callService(String uri, HttpHeaders header, HttpMethod httpMethod) throws URISyntaxException {
        // 응답 객체
        T responseMessage = null;

        try {
            ResponseEntity<?> responseEntity = restTemplate.exchange(
                    new URI(uri),
                    httpMethod,
                    new HttpEntity<>(header),
                    String.class
            );

            responseMessage = (T) new ObjectMapper().convertValue(responseEntity.getBody(), new TypeReference<T>(){});

            if ( responseEntity.getStatusCode() != HttpStatus.OK ) {
                log.error("ErrorMessage : " + responseEntity.getStatusCode());
                throw new HttpServerErrorException(responseEntity.getStatusCode());
            }

        } catch (URISyntaxException e) {
            log.error("URL Encoding 중에 오류가 발생하였습니다.", e);
            throw e;
        }

        return (T) responseMessage;
    }

    /**
     final HttpHeaders headers = new HttpHeaders();
     * create Header
     * @return
     */
    private HttpHeaders createHeader() {
        final HttpHeaders headers = new HttpHeaders();

        return headers;
    }

}
