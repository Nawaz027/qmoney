
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {
// returns the symbol from trades.json file
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper objectMapper = getObjectMapper();
    PortfolioTrade[] trades = objectMapper.readValue(file, PortfolioTrade[].class);
    List<String> symbols = new ArrayList<String>();
    for(PortfolioTrade stock : trades){
      symbols.add(stock.getSymbol());
    }
     return symbols;
  }

  private static String readFileAsString(String filename) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(resolveFileFromResources(filename).toPath()), "UTF-8");
  }



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

 // returns the fileName
  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  // creates a objectMapper
  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  

  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "trades.json";
     String toStringOfObjectMapper = "ObjectMapper";
     String functionNameFromTestFileInStackTrace = "mainReadFile";
     String lineNumberFromTestFileInStackTrace = "";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  //api Endpoint
  //https://api.tiingo.com/tiingo/daily/goog/prices?startDate=2019-01-02&token=b5f3dce68addb97433401fb51a54cfe4689aa3e1
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(filename);
    ObjectMapper objectmapper = getObjectMapper();
    PortfolioTrade[] portfolioTrade = objectmapper.readValue(file, PortfolioTrade[].class);
    List<PortfolioTrade> listPortfolioTrade = Arrays.asList(portfolioTrade);
    return listPortfolioTrade;
  }

  //get token for API endpoint
  public static String getToken() {
    return "289464e8faf5cf34aba42001442fb59b3c854b6c";
  }

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    List<TotalReturnsDto> totalReturnsDto = new ArrayList<TotalReturnsDto>();
    List<PortfolioTrade> portfolioTrade = readTradesFromJson(args[0]);
    for(PortfolioTrade t : portfolioTrade){
      LocalDate endDate = LocalDate.parse(args[1]);
      String url = prepareUrl(t, endDate, getToken());
      System.out.println(url);
      TiingoCandle[] results = restTemplate.getForObject(url, TiingoCandle[].class);
      if(results != null){
        totalReturnsDto.add(new TotalReturnsDto(t.getSymbol(), results[results.length - 1].getClose()));
      }
    }
    
    Collections.sort(totalReturnsDto, new Comparator<TotalReturnsDto>(){
      
      @Override
      public int compare(TotalReturnsDto o1, TotalReturnsDto o2){
        return (int) (o1.getClosingPrice().compareTo(o2.getClosingPrice()));
      }

    });

    List<String> listAnswer = new ArrayList<>();
    for(int i = 0; i < totalReturnsDto.size(); i++){
      listAnswer.add(totalReturnsDto.get(i).getSymbol());
    }
    return listAnswer;
  };
    
  
// returns the API endpoint URL
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    return  "https://api.tiingo.com/tiingo/daily/"+ trade.getSymbol() +"/prices?startDate="+ 
    trade.getPurchaseDate() +"&endDate="+ endDate +"&token=" + token;
  }

  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
    return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    List <Candle> listCandle1 = new ArrayList<Candle>();
    String url = prepareUrl(trade, endDate, token);

    RestTemplate restTemplate = new RestTemplate();
    TiingoCandle[] tiingoCandle = restTemplate.getForObject(url, TiingoCandle[].class);

    for (TiingoCandle i: tiingoCandle) {
      listCandle1.add(i);
    }
     return listCandle1;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
        List <AnnualizedReturn> annualizedReturns = new ArrayList<>();
        LocalDate endDate = LocalDate.parse(args[1]);
        

        List<PortfolioTrade> portfolioTrades = new ArrayList<>();
        portfolioTrades = readTradesFromJson(args[0]);
        

        for (PortfolioTrade trade: portfolioTrades) {
          LocalDate startDate = trade.getPurchaseDate();
          if (startDate.compareTo(endDate) >= 0) {
            throw new RuntimeException();
          }

          List<Candle> candle = fetchCandles(trade, endDate, getToken());

          annualizedReturns.add(calculateAnnualizedReturns(endDate, trade, getOpeningPriceOnStartDate(candle), getClosingPriceOnEndDate(candle)));

        }

        Comparator<AnnualizedReturn> sortByAnnualReturn = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
        Collections.sort(annualizedReturns, sortByAnnualReturn);
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


  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
      //  String file = args[0];
      File file = resolveFileFromResources(args[0]);
       LocalDate endDate = LocalDate.parse(args[1]);
      //  String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(file, PortfolioTrade[].class);
      //  RestTemplate restTemplate;
      PortfolioManager portfolioManager = new PortfolioManagerImpl(new RestTemplate());
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    // printJsonObject(mainCalculateSingleReturn(args));
    // printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}


