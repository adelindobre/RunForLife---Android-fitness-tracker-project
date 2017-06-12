package facebook_utils;

/**
 * Created by AdelinGDobre on 5/11/2017.
 */

public class AppConstants {
    public enum SharedPreferenceKeys {
        USER_NAME("userName"),
        USER_EMAIL("userEmail"),
        USER_IMAGE_URL("userImageUrl");

        private String value;

        SharedPreferenceKeys(String value) {
            this.value = value;
        }

        public String getKey() {
            return value;
        }

    }
}