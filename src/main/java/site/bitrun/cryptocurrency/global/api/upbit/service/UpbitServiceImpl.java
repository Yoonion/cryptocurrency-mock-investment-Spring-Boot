package site.bitrun.cryptocurrency.global.api.upbit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import site.bitrun.cryptocurrency.global.api.coinmarketcap.domain.CryptoRank;
import site.bitrun.cryptocurrency.global.api.upbit.domain.UpbitMarket;
import site.bitrun.cryptocurrency.global.api.upbit.dto.UpbitMarketDto;
import site.bitrun.cryptocurrency.global.api.upbit.repository.UpbitRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UpbitServiceImpl implements UpbitService {

    private final UpbitRepository upbitRepository;

    @Autowired
    public UpbitServiceImpl(UpbitRepository upbitRepository) {
        this.upbitRepository = upbitRepository;
    }

    @Override
    public void saveUpbitMarket() {
        /**
         * 업비트 거래 가능 목록 API 저장
         */
        String upbitRequestUrl = "https://api.upbit.com/v1/market/all";

        RestTemplate restTemplate = new RestTemplate();
        String responseData = restTemplate.getForObject(upbitRequestUrl, String.class);
        // System.out.println("responseData = " + responseData);

        // Jackson lib
        ObjectMapper objectMapper = new ObjectMapper();

        List<UpbitMarketDto> upbitMarketDtos = null;
        try {
            UpbitMarketDto[] upbitMarketDtoArray = objectMapper.readValue(responseData, UpbitMarketDto[].class);
            upbitMarketDtos = Arrays.asList(upbitMarketDtoArray);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        /**
         * LIST 에 담아 SAVE ALL
         */
        List<UpbitMarket> upbitMarkets = new ArrayList<>();
        for (UpbitMarketDto upbitMarketDto : upbitMarketDtos) {

            UpbitMarket upbitMarket = new UpbitMarket();
            upbitMarket.setMarket(upbitMarketDto.getMarketCode());
            upbitMarket.setKoreanName(upbitMarketDto.getKoreanName());
            upbitMarket.setEnglishName(upbitMarketDto.getEnglishName());

            upbitMarkets.add(upbitMarket);
        }

         upbitRepository.saveAll(upbitMarkets);
    }

    @Override
    public List<UpbitMarket> getUpbitMarketList() {
        return upbitRepository.findByMarketLike("KRW%");
    }

    @Override
    public UpbitMarket getUpbitMarketOne(String code) {
        return upbitRepository.findByMarket(code);
    }
}
