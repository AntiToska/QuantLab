package com.quantlab.analytics;

import com.quantlab.common.config.QuantLabProperties;
import com.quantlab.marketdata.connector.MarketDataEventPublisher;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.Instrument;
import com.quantlab.marketdata.model.KlineEvent;
import com.quantlab.marketdata.model.KlineInterval;
import java.math.BigDecimal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地研究样例数据灌库器。
 * <p>
 * 只用于本地联调，把最小一段连续 K 线写入 PostgreSQL，
 * 让回测和研究 runner 有可消费的数据。
 */
@Component
@ConditionalOnProperty(prefix = "quantlab.research.seed-data", name = "enabled", havingValue = "true")
public class ResearchSeedDataRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ResearchSeedDataRunner.class);

    private final QuantLabProperties properties;
    private final MarketDataEventPublisher publisher;

    public ResearchSeedDataRunner(QuantLabProperties properties, MarketDataEventPublisher publisher) {
        this.properties = properties;
        this.publisher = publisher;
    }

    @Override
    public void run(ApplicationArguments args) {
        QuantLabProperties.SeedDataProperties config = properties.research().seedData();
        Instrument instrument = new Instrument(
                Exchange.fromValue(required(config.exchange(), "exchange")),
                required(config.symbol(), "symbol")
        );
        KlineInterval interval = KlineInterval.valueOf(required(config.interval(), "interval").trim().toUpperCase());
        Instant fromInclusive = Instant.parse(required(config.fromInclusive(), "fromInclusive"));
        BigDecimal price = new BigDecimal(required(config.startPrice(), "startPrice"));
        BigDecimal priceStep = new BigDecimal(required(config.priceStep(), "priceStep"));
        BigDecimal volume = new BigDecimal(required(config.volume(), "volume"));

        for (int index = 0; index < config.bars(); index++) {
            Instant openTime = openTime(interval, fromInclusive, index);
            Instant closeTime = closeTime(interval, openTime);
            BigDecimal openPrice = price;
            BigDecimal closePrice = price.add(priceStep);
            BigDecimal highPrice = openPrice.max(closePrice);
            BigDecimal lowPrice = openPrice.min(closePrice);

            publisher.publish(new KlineEvent(
                    instrument,
                    closeTime,
                    closeTime,
                    interval,
                    openTime,
                    closeTime,
                    openPrice,
                    highPrice,
                    lowPrice,
                    closePrice,
                    volume,
                    true
            ));
            price = closePrice;
        }

        log.info(
                "Seeded research kline data. exchange={}, symbol={}, interval={}, bars={}",
                instrument.exchange(),
                instrument.symbol(),
                interval,
                config.bars()
        );
    }

    private Instant openTime(KlineInterval interval, Instant fromInclusive, int index) {
        return switch (interval) {
            case ONE_MINUTE -> fromInclusive.plusSeconds(index * 60L);
            case FIVE_MINUTES -> fromInclusive.plusSeconds(index * 300L);
            case FIFTEEN_MINUTES -> fromInclusive.plusSeconds(index * 900L);
            case ONE_HOUR -> fromInclusive.plusSeconds(index * 3600L);
            case FOUR_HOURS -> fromInclusive.plusSeconds(index * 14400L);
            case ONE_DAY -> fromInclusive.plusSeconds(index * 86400L);
        };
    }

    private Instant closeTime(KlineInterval interval, Instant openTime) {
        return switch (interval) {
            case ONE_MINUTE -> openTime.plusSeconds(59L);
            case FIVE_MINUTES -> openTime.plusSeconds(299L);
            case FIFTEEN_MINUTES -> openTime.plusSeconds(899L);
            case ONE_HOUR -> openTime.plusSeconds(3599L);
            case FOUR_HOURS -> openTime.plusSeconds(14399L);
            case ONE_DAY -> openTime.plusSeconds(86399L);
        };
    }

    private String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
