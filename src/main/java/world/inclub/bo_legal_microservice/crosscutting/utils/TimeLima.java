package world.inclub.bo_legal_microservice.crosscutting.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeLima {

    private static final ZoneId LIMA_ZONE_ID = ZoneId.of("America/Lima");

    /**
     * Converts the given UTC time to Lima, Peru time.
     *
     * @param utcTime the UTC time to convert
     * @return the converted LocalDateTime in Lima time zone
     */
    public static LocalDateTime convertUtcToLimaTime(ZonedDateTime utcTime) {
        return utcTime.withZoneSameInstant(LIMA_ZONE_ID).toLocalDateTime();
    }

    /**
     * Gets the current time in Lima, Peru time zone.
     *
     * @return the current LocalDateTime in Lima time zone
     */
    public static LocalDateTime getLimaTime() {
        return ZonedDateTime.now(ZoneId.of("UTC")).withZoneSameInstant(LIMA_ZONE_ID).toLocalDateTime();
    }
}