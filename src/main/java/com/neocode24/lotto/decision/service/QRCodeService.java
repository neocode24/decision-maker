package com.neocode24.lotto.decision.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class QRCodeService {

    /**
     * QR Code가 포함된 Image 에서 식별해서, QRCode가 제공하는 URL을 획득함.
     * @param bufferedImage
     * @return
     * @throws NotFoundException
     */
    public String decodeQRCode(BufferedImage bufferedImage) throws NotFoundException {
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(DecodeHintType.TRY_HARDER, true);

        Result result = new MultiFormatReader().decode(bitmap, hintMap);
        return result.getText();
    }
}
