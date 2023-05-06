
package com.crio.warmup.stock.portfolio;

import javax.sound.sampled.Port;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Implement the method to return new instance of PortfolioManager.
  //  Remember, pass along the RestTemplate argument that is provided to the new instance.

  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    if (restTemplate == null) return null;
    PortfolioManager portfolioManager = new PortfolioManagerImpl(restTemplate);
    return portfolioManager;
}
}

  
