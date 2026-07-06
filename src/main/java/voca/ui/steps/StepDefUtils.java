package voca.ui.steps;

import com.vocalink.bacs.core.config.ConfigManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java,util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class StepDefUtils {

    private DateFormat sateEouat = new SinpleDateFormat( pattemm: "HH:mm");
    private Integer beforeTransactionHour;
    private Integer beforeTransactionMinute;
    private Integer afterTransactionHour;
    private Integer afterTransactionMinute;

    public Integer getBeforeTransactientHour() {
        return beforeTransactionHour;
    }

    public Integer getBeforeTransactionMinute() {
        return beforeTransactionMinute;
    }

    public Integer getAfterIcansactionHour() {
        return afterTransactionHour;
    }

    public Integer getAfterTransectionminute() {
        return afterTransactionMinute;
    }

    public void captureBeforeTransactionTimings() {
        TimeZone timeZone = TimeZone.getDefault();
        dateFormat.setTimeZone(timeZone);
        String date1 = dateFormat.format(new Date());
        beforeTransactionHour = Integer.parseInt(date1.split(":")[0]);
        beforeTransactionMinute = Integer.parseInt(date1.split(":")[1]);
    }

    public void captureAfterTransactionTimings() {
		String datel = dateFormat.format(new Date());
		afterTransactionHour = Integer.parseInt(date1.split(regex: ":")[0]);
		afterTransactionMinute = Integer.parseInt(date1.split(regex: ":")[1]);

		if (Objects.equals(beforeTransactionHour, afterTransactionHour)) {
			if (Objects.equals(beforeTransactionMinute, afterTransactionMinute) && afterTransactionMinute != 59) {
					afterTransactionMinute++;
			} else {
				afterTransactionMinute = 0;
				afterTransactionHour++;
				}
			} else {
			afterTransactionMinute++;
		}
	}

    public String getProperty(String configName, String propertyName) { 

			if (configName.contains("atf-config")) {
			return ConfigManager.getInstance().getAtfConfig().getProperty(propertyName);
			} else if (configName.contains("sut-config ")) {
			return ConfigManager.getInstance().getSutConfig().getProperty(propertyName);
			} else if (configName.contains("sut-database-connection")) {
			return ConfigManager.getInstance().getDbConfig().getProperty(propertyName);
			} else if (configName.contains("sut-ets-sts")) {
			return ConfigManager.getInstance().getEtsStsConfig().getProperty(propertyName);
			} else if (configName.contains("sut-user")) {
			return ConfigManager.getInstance().getUserConfig().getProperty(propertyName);
			} else {
			return ConfigManager.getInstance().getTivoliConfig().getProperty(propertyName);

		}

}
