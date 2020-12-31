package com.neocode24.lotto.decision;

import com.google.zxing.NotFoundException;
import com.neocode24.lotto.decision.service.DecisionService;
import com.neocode24.lotto.decision.domain.LottoDecisionDomain;
import com.neocode24.lotto.decision.service.QRCodeService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Log4j2
@RestController
public class DecisionController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private DecisionService decisionService;


    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        String message = "";

        if (file.isEmpty()) {
            log.info("이미지 파일없이 생성 요청되었습니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미지 파일이 전송되지 않았습니다.");
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());

            LottoDecisionDomain lottoDecisionDomain = decisionService.getLottoHtmlDataFromServer(
                    qrCodeService.decodeQRCode(bufferedImage),
                    getClinetIp(request)
            );

            log.info("Lotto 발행이 완료되었습니다. {}", lottoDecisionDomain);

            return ResponseEntity.status(HttpStatus.OK).body(lottoDecisionDomain);

        } catch (IOException | NotFoundException e) {
            log.error("QR Code 인식에 실패 하였습니다.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("QR Code 인식에 실패 하였습니다.");

        } catch (DataAccessResourceFailureException e) {
            log.error("데이터 저장중에 오류가 발생되었습니다.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("서버 오류가 발생되었습니다. 잠시 후 다시 시도하시기 바랍니다.");
        } catch (Exception e) {
            log.error("서버 오류가 발생되었습니다. 잠시 후 다시 시도하시기 바랍니다.", e);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(e.getMessage());
        }
    }

    /**
     * Client IP 획득
     * @param request
     * @return
     */
    private String getClinetIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        return String.format("%s", ip);
    }
}
