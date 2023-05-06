
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  public static final String APIKEY = "289464e8faf5cf34aba42001442fb59b3c854b6c";
  public RestTemplate restTemplate;
  public PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF

 public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
  LocalDate endDate) {
    List <AnnualizedReturn> annualizedReturns = new ArrayList<>();    
    

    for (PortfolioTrade trade: portfolioTrades) {
      LocalDate startDate = trade.getPurchaseDate();
      if (startDate.compareTo(endDate) >= 0) {
        throw new RuntimeException();
      }

      List<Candle> candle = new ArrayList<Candle>();
      try {
        candle = getStockQuote(trade.getSymbol(), startDate, endDate);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }

      annualizedReturns.add(calculateAnnualizedReturns(endDate, trade, 
      getOpeningPriceOnStartDate(candle), getClosingPriceOnEndDate(candle)));
    }
    Collections.sort(annualizedReturns, getComparator());
    return annualizedReturns;
  }

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
  PortfolioTrade trade, Double buyPrice, Double sellPrice) {
    Double absReturn  = (sellPrice - buyPrice)/ buyPrice;
    String symbol = trade.getSymbol();
    LocalDate purchaseDate = trade.getPurchaseDate();

    Double numYears = (double) ChronoUnit.DAYS.between(purchaseDate, endDate)/365;
    Double annualizedReturns = Math.pow((1+ absReturn), (1/ numYears)) - 1;
    return new AnnualizedReturn(symbol, annualizedReturns, absReturn);
}


  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }
  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
  }

  public List<Candle> getStockQuote(String symbol, LocalDate startDate, LocalDate endDate)
      throws JsonProcessingException {
        List <Candle> listCandle1 = new ArrayList<Candle>();
        String url = buildUri(symbol, startDate, endDate);
    
        RestTemplate restTemplate = new RestTemplate();
        TiingoCandle[] tiingoCandle = restTemplate.getForObject(url, TiingoCandle[].class);
    
        for (TiingoCandle i: tiingoCandle) {
          listCandle1.add(i);
        }
         return listCandle1;
  }

  // protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
  //      String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
  //           + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
  //           return uriTemplate;
  // }

  public static String getToken() {
    return "289464e8faf5cf34aba42001442fb59b3c854b6c";
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
      return "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate 
      + "&endDate=" + endDate + "&token=" + getToken();
    }
}
