package streaming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.akp.streaming.TransactionTopology;

class TransactionTopologyTest {

    private TopologyTestDriver driver;
    private TestInputTopic<String, Double> transactions;
    private TestOutputTopic<String, Double> largeTransactions;
    private TestOutputTopic<String, Double> accountTotals;
    private TestOutputTopic<String, Long> fraudlentAlerts;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        driver = new TopologyTestDriver(TransactionTopology.build(), props);

        transactions = driver.createInputTopic(TransactionTopology.TRANSACTIONS, new StringSerializer(),
                new DoubleSerializer());
        largeTransactions = driver.createOutputTopic(TransactionTopology.LARGE_TRANSACTIONS,
                new StringDeserializer(), new DoubleDeserializer());
        accountTotals = driver.createOutputTopic(TransactionTopology.ACCOUNT_TOTALS, new StringDeserializer(),
                new DoubleDeserializer());
        fraudlentAlerts = driver.createOutputTopic(TransactionTopology.FRAUDLENT_ALERTS, new StringDeserializer(),
                new LongDeserializer());
    }

    @Test
    void topologyTest() {
        System.out.println(TransactionTopology.build().describe());
    }

    @Test
    void transactionOf10000OrMoreIsForwardedToLargeTransactions() {
        transactions.pipeInput("acc-1", 15000.0);
        System.out.println(TransactionTopology.build().describe());
        assertEquals(new KeyValue<>("acc-1", 15000.0), largeTransactions.readKeyValue());
    }

    @Test
    void transactionBelow10000IsNotForwarded() {
        transactions.pipeInput("acc-1", 9999.9);
        assertTrue(largeTransactions.isEmpty());
    }

    @Test
    void transactionOfExactly10000IsForwarded() {
        transactions.pipeInput("acc-1", 10000.0);
        assertEquals(new KeyValue<>("acc-1", 10000.0), largeTransactions.readKeyValue());
    }

    @Test
    void firstTransactionPublishesItsAmountAsTheAccountTotal() {
        transactions.pipeInput("acc-1", 200.0);
        assertEquals(new KeyValue<>("acc-1", 200.0), accountTotals.readKeyValue());
    }

    @Test
    void eachNewTransactionPublishesTheUpdatedAccountTotal() {
        transactions.pipeInput("acc-1", 200.0);
        transactions.pipeInput("acc-1", 300.0);

        assertEquals(
                List.of(new KeyValue<>("acc-1", 200.0), new KeyValue<>("acc-1", 500.0)),
                accountTotals.readKeyValuesToList());
    }

    @Test
    void eachAccountKeepsItOwnTotal() {
        transactions.pipeInput("acc-1", 200.0);
        transactions.pipeInput("acc-2", 50.0);
        transactions.pipeInput("acc-1", 300.0);
        assertEquals(List.of(new KeyValue<>("acc-1", 200.0),
                new KeyValue<>("acc-2", 50.0),
                new KeyValue<>("acc-1", 500.0)),
                accountTotals.readKeyValuesToList());
    }

    @Test
    void threeLargeTransactionsWithinFiveMinutesWindowRaiseAlert() {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");

        transactions.pipeInput("acc-1", 10000.0, start);
        transactions.pipeInput("acc-1", 14000.0, start.plus(Duration.ofMinutes(1)));
        transactions.pipeInput("acc-1", 17000.0, start.plus(Duration.ofMinutes(2)));
        assertEquals(new KeyValue<>("acc-1", 3l), fraudlentAlerts.readKeyValue());
    }

    @Test
    void threeTransactionsSpreadOverMoreThanFiveMinutesShouldNotRaiseAlert() {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        transactions.pipeInput("acc-1", 10000.0, start);
        transactions.pipeInput("acc-1", 12000.0, start.plus(Duration.ofMinutes(3)));
        transactions.pipeInput("acc-1", 13000.0, start.plus(Duration.ofMinutes(6)));
        assertTrue(fraudlentAlerts.isEmpty());
    }

    @Test
    void furtherLargeTransactionsDuringCoolDownPeriodShouldNotRaiseAlert() {
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        transactions.pipeInput("acc-1", 12000.0, start);
        transactions.pipeInput("acc-1", 15000.0, start.plus(Duration.ofMinutes(1)));
        transactions.pipeInput("acc-1", 17000.0, start.plus(Duration.ofMinutes(2)));
        transactions.pipeInput("acc-1", 11000.0, start.plus(Duration.ofMinutes(3)));
        assertEquals(List.of(new KeyValue<>("acc-1", 3L)), fraudlentAlerts.readKeyValuesToList());
    }

    @AfterEach
    void tearDownn() {
        driver.close();
    }

}
