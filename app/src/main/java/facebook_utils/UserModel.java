package facebook_utils;

/**
 * Created by AdelinGDobre on 5/11/2017.
 */


import android.os.Parcel;
import android.os.Parcelable;


public class UserModel implements  Parcelable {
    public String userName;
    public String userEmail;
    public String profilePic;
    public String gender;
    public String password;

    public static final Parcelable.Creator<UserModel> CREATOR = new Parcelable.Creator<UserModel>() {

        @Override
        public UserModel createFromParcel(Parcel parcel) {
            return new UserModel(parcel);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public UserModel() {
    }

    public UserModel(Parcel parcel) {
        userName = parcel.readString();
        userEmail = parcel.readString();
        profilePic = parcel.readString();
        gender = parcel.readString();
        password = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(userName);
        parcel.writeString(userEmail);
        parcel.writeString(profilePic);
        parcel.writeString(gender);
        parcel.writeString(password);
    }
}
