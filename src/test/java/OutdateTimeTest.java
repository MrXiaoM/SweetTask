import org.junit.Test;
import top.mrxiaom.sweet.taskplugin.tasks.EnumTaskType;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static top.mrxiaom.sweet.taskplugin.func.TaskManager.nextOutdate;

public class OutdateTimeTest {
    static class TestUnit {
        LocalDateTime now;
        EnumTaskType type;
        LocalTime resetTime;
        int expectYear, expectMonth, expectDate, expectHour;
        public TestUnit(LocalDateTime now, EnumTaskType type, int expectYear, int expectMonth, int expectDate, int expectHour) {
            this.now = now;
            this.type = type;
            this.expectYear = expectYear;
            this.expectMonth = expectMonth;
            this.expectDate = expectDate;
            this.expectHour = expectHour;
        }

        public void setResetTime(LocalTime resetTime) {
            this.resetTime = resetTime;
        }

        public void test() {
            LocalDateTime next = nextOutdate(now, type, resetTime);
            if (next.getYear() == expectYear && next.getMonthValue() == expectMonth && next.getDayOfMonth() == expectDate && next.getHour() == expectHour) {
                System.out.println("[测试通过] " + now + " " + type + " -> " + next);
            } else {
                System.out.println("[测试失败] " + now + " " + type + " -> " + next);
                throw new IllegalStateException("对于 " + type + " 类型，如果当前时间是 " + now + "，到期时间应为 " + expectYear + "-" + expectMonth + "-" + expectDate + "T" + expectHour + ":*:*");
            }
        }
    }

    @Test
    public void verifyNextOutdate() {
        LocalDateTime now;
        LocalTime resetTime = LocalTime.of(4, 0);
        List<TestUnit> tests = new ArrayList<>();
        ///////////// DAILY ///////////////
        now = LocalDateTime.of(2025, 5, 21, 6, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.DAILY, 2025, 5, 22, 4));

        now = LocalDateTime.of(2025, 5, 21, 3, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.DAILY, 2025, 5, 21, 4));

        now = LocalDateTime.of(2025, 5, 21, 4, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.DAILY, 2025, 5, 21, 4));

        now = LocalDateTime.of(2025, 5, 21, 4, 0, 1);
        tests.add(new TestUnit(now, EnumTaskType.DAILY, 2025, 5, 22, 4));

        ///////////// MONTHLY ///////////////
        now = LocalDateTime.of(2025, 6, 1, 3, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.MONTHLY, 2025, 6, 1, 4));

        now = LocalDateTime.of(2025, 6, 1, 6, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.MONTHLY, 2025, 7, 1, 4));

        now = LocalDateTime.of(2025, 6, 1, 4, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.MONTHLY, 2025, 6, 1, 4));

        now = LocalDateTime.of(2025, 6, 1, 4, 0, 1);
        tests.add(new TestUnit(now, EnumTaskType.MONTHLY, 2025, 7, 1, 4));

        ////////////// WEEKLY ///////////////
        // 5月19日是星期一，下一个星期一是5月26日
        now = LocalDateTime.of(2025, 5, 19, 3, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.WEEKLY, 2025, 5, 19, 4));

        now = LocalDateTime.of(2025, 5, 19, 6, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.WEEKLY, 2025, 5, 26, 4));

        now = LocalDateTime.of(2025, 5, 19, 4, 0, 0);
        tests.add(new TestUnit(now, EnumTaskType.WEEKLY, 2025, 5, 19, 4));

        now = LocalDateTime.of(2025, 5, 19, 4, 0, 1);
        tests.add(new TestUnit(now, EnumTaskType.WEEKLY, 2025, 5, 26, 4));

        for (TestUnit test : tests) {
            test.setResetTime(resetTime);
            test.test();
        }
    }
}
