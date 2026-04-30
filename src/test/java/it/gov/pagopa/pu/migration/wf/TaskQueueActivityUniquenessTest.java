package it.gov.pagopa.pu.migration.wf;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.temporal.spring.boot.autoconfigure.template.WorkersTemplate;
import it.gov.pagopa.pu.migration.utils.MemoryAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@TestPropertySource(properties = {
  "spring.datasource.driver-class-name=org.h2.Driver",
  "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
  "spring.datasource.username=sa",
  "spring.datasource.password=sa",

  "spring.cloud.function.definition=",

  "spring.temporal.test-server.enabled: true"
})
class TaskQueueActivityUniquenessTest {

  @Autowired
  private WorkersTemplate workersTemplate;

  private static MemoryAppender memoryAppender;

  @BeforeEach
  void registerAppender(){
    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(WorkersTemplate.class);
    memoryAppender = new MemoryAppender();
    memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
    logger.setLevel(ch.qos.logback.classic.Level.INFO);
    logger.addAppender(memoryAppender);
    memoryAppender.start();
  }

  /** Auto-discovery mode will ignore duplicate activities, only explicit configuration is throwing an error if it occurs */
  @Test
  void testUniquenessWhenAutoDiscovery() {
    resetWorkers();

    workersTemplate.getWorkers();

    Assertions.assertEquals(List.of(),
      memoryAppender.getLoggedEvents().stream()
        .map(ILoggingEvent::getMessage)
        .filter(m -> m.startsWith("Skipping auto-discovered"))
        .toList()
    );
  }

  private void resetWorkers() {
    setFieldToNull("workers");
    setFieldToNull("workerFactory");
    setFieldToNull("testWorkflowEnvironment");
  }

  private void setFieldToNull(String workers) {
    Field workersField = Objects.requireNonNull(ReflectionUtils.findField(WorkersTemplate.class, workers));
    workersField.setAccessible(true);
    ReflectionUtils.setField(workersField, workersTemplate, null);
  }
}
