package com.deadlock.tenpencearcade.ten_p_episodes_mag;

public class Episode
{
	public final static String DEFAULT_IMAGE = "https://i0.wp.com/tenpencearcade.co.uk/wp-content/uploads/2017/07/cropped-10p-FB-Cover-July-2017.png";
	private String title		= "";
	private String description	= "";
	private String image		= "";
	private String mp3			= "";
	private String readMoreLink	= "";
	private String dateStamp	= "";

	private Episode(){}

	public Episode(String title, String description, String image, String mp3, String readMore, String dateStamp)
	{
		this();
		this.setTitle(title);

		if (description.indexOf("Ribbit") != -1)
			description = "Ribbit,Ribbit,Ribbit,Ribbit,Ribbit,Ribbit,Ribbit. Biscuits.";

		this.setDescription(description);
		if (image == null || image.isEmpty())
			image = DEFAULT_IMAGE;
		this.setImage(image);
		this.setMp3(mp3);
		this.setReadMoreLink(readMore);
		this.setDateStamp(dateStamp);
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getImage() {
		return image;
	}

	public String getMp3() {
		return mp3;
	}

	private void setTitle(String title) {
		this.title = title;
	}

	private void setDescription(String description) {
		this.description = description + "...";
	}

	private void setImage(String image) {
		this.image = image;
	}

	private void setMp3(String mp3) {
		this.mp3 = mp3;
	}

	public String getReadMoreLink() {
		return readMoreLink;
	}

	private void setReadMoreLink(String readMoreLink) {
		this.readMoreLink = readMoreLink;
	}

	public String getDateStamp() {
		return dateStamp;
	}

	private void setDateStamp(String dateStamp) {
		this.dateStamp = dateStamp;
	}


}
