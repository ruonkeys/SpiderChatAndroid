package rahul.com.spiderchat;

/**
 * Created by rahul on 27/7/17.
 */

public class UserData {
    private String image;
    private String name;
    private String status;
    private String thumb_image;

    UserData()
    {

    }
    UserData(String image,String name,String status,String thumb_image)
    {
        this.image = image;
        this.name = name;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
