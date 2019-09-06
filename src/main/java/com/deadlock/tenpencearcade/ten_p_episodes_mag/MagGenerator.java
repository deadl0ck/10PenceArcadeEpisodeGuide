package com.deadlock.tenpencearcade.ten_p_episodes_mag;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

public class MagGenerator
{
    public final static String START_URL			= "http://tenpencearcade.co.uk";
    public final static String ARTICLE_TAG			= "article";
    public final static String PARAGRAPH_TAG		= "p";
    public final static String SELECT_TAG			= "select";
    public final static String ANCHOR_TAG			= "a";
    public final static String IMAGE_TAG			= "img";
    public final static String HREF					= "href";
    public final static String SOURCE				= "src";
    public final static String DIV					= "div";
    public final static String ARCHIVES_DROPDOWN_ID	= "archives-dropdown-2";
    public final static String PDF_NAME				= "/Users/martinstephenson/Desktop/10p Episode Guide.pdf";
    public final static double IMAGE_SCALE			= 0.85;
    public final static double LARGE_IMAGE_SCALE	= 0.55;
    public final static String IMAGE_CLASS			= "wp-post-image-link";
    public final static String PARAMETER_MARKER		= "?";
    public final static String H_2_TAG				= "h2";
    public final static String H_1_TAG				= "h1";
    public final static String ARCHIVE_TITLE		= "archive-title";
    public final static String TITLE_CLASS			= "entry-title";
    public final static String MP3_CLASS			= "powerpress_links powerpress_links_mp3";
    public final static String READ_MORE_CLASS		= "read-more";
    public final static int NUM_CONTENT_ITEMS_PER_PAGE	= 40;

    // Use these for debug to build a smaller PDF
    public final static boolean FULL_PDF				= true;
    public final static String PARTIAL_PDF_PATTERN		= "2019/";

    public static void main(String[] args) throws IOException, DocumentException
    {
    	debugOut("Starting to process....");
        MagGenerator gen = new MagGenerator();
        gen.process();
        debugOut("Processing complete - PDF is available at: " + PDF_NAME);
    }

    private List<Page> getPageList() throws IOException
    {
    	List<Page> pages = new ArrayList<Page>();

        Elements body = this.getElementsFromHtml(START_URL);

        // Get the tag and ID we are interested in
        Element episodesDropdown = this.getTagAndIdAndChildren(body, SELECT_TAG, ARCHIVES_DROPDOWN_ID);
        Elements children = episodesDropdown.children();
        for (Element child: children)
        {
        	String value = child.val();
        	if (!value.isEmpty())
        		pages.add(new Page(value, child.ownText()));
        }
        return pages;
    }

    private String getTitle(Element article)
    {
    	Element title = this.getTagAndClassAndChildren(article.children(), H_2_TAG, TITLE_CLASS);
    	return title.text();
    }

    private String getDescription(Element article)
    {
    	Elements elements = article.select(PARAGRAPH_TAG);
    	for (Element e: elements)
    		if (e.className().isEmpty())
    			return e.text();
    	return "";
    }

    private String getMP3Link(Element article)
    {
    	Element mp3 = this.getTagAndClassAndChildren(article.children(), PARAGRAPH_TAG, MP3_CLASS);
    	if (mp3 == null)
    		return "None Available";
    	Elements anchors = mp3.select(ANCHOR_TAG);
    	for (Element anchor: anchors)
    	{
    		String url = anchor.attr(HREF);
    		if (url.endsWith(".mp3"))
    			return url;
    	}
    	return "";
    }

    private String getReadMoreLink(Element article)
    {
    	Element moreLink = this.getTagAndClassAndChildren(article.children(), DIV, READ_MORE_CLASS);
    	Elements anchors = moreLink.select(ANCHOR_TAG);
    	return anchors.get(0).attr(HREF);
    }

    private String getImage(Element article)
    {
    	Elements children = article.children();
    	for (Element child: children)
    	{
    		if (child.className().equals(IMAGE_CLASS))
    		{
    			Elements images = child.select(IMAGE_TAG);
    			for (Element image: images)
    			{
    				String im = image.attr(SOURCE);
    				if (im != null)
    				{
    					// Remove any parameters
    					int pos = im.indexOf(PARAMETER_MARKER);
    					if (pos != -1)
    						return im.substring(0, pos);
    					return im;
    				}
    			}
    		}
    	}
    	return null;
    }

//    private String getFullDescriptionText(Element article) throws IOException
//    {
//    	Element moreLink = this.getTagAndClassAndChildren(article.children(), DIV, READ_MORE_CLASS);
//    	Elements anchors = moreLink.select(ANCHOR_TAG);
//    	for (Element anchor: anchors)
//    	{
//    		String url = anchor.attr(HREF);
//    		if (url != null && !url.isEmpty())
//    		{
//    			Elements elements = this.getElementsFromHtml(url);
//    			Element desc = this.getTagAndClassAndChildren(elements, DIV, "entry-content clearfix");
//    			Elements paragraphs = desc.select(PARAGRAPH_TAG);
//    			String fullDesc = "";
//    			for (Element paragraph : paragraphs)
//    			{
//    				Elements spans = paragraph.select("span");
//    				if (spans.size() > 0)
//    					break;
//    				String pClass = paragraph.className();
//    				if (!pClass.isEmpty())
//    					continue;
//    				fullDesc += paragraph.ownText();
//    			}
//    			System.out.println("Full Desc: " + fullDesc);
//
//    			// Check for TIMESTAMP
//    			int pos = fullDesc.indexOf("TIMESTAMP");
//    			if (pos != -1)
//    				fullDesc = fullDesc.substring(0, pos);
//    			return fullDesc;
//    		}
//    	}
//
//    	return "";
//    }

    private Elements getElementsFromHtml(String url) throws IOException
    {
    	String html = this.getHtml(url);
        Document doc = Jsoup.parse(html);
        Elements body = doc.body().children();
        return body;
    }

    private Elements getArticles(String url) throws IOException
    {
        Elements body = this.getElementsFromHtml(url);
        Elements articles = body.select(ARTICLE_TAG);
    	return articles;
    }

    private String getDateText()
    {
    	DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy");
    	Date date = new Date();
    	return dateFormat.format(date);
    }

    public static String getTimestamp()
    {
    	DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	Date date = new Date();
    	return "[" + dateFormat.format(date) + "]";
    }

    private boolean skipPodcast(String url)
    {
    	if (!FULL_PDF)
    		if (url.indexOf(PARTIAL_PDF_PATTERN) == -1)
    			return true;
    	return false;
    }

    private static void debugOut(String text)
    {
    	System.out.println(MagGenerator.getTimestamp() + text);
    }

    private void process() throws IOException, DocumentException
    {
    	List<Episode> episodes = new ArrayList<Episode>();
    	List<Page> pages = this.getPageList();
    	for (Page page: pages)
    	{
    		if (this.skipPodcast(page.getUrl()))
    			continue;
    		debugOut("Processing URL: :" + page.getUrl());
    		Elements articles = this.getArticles(page.getUrl());
    		for (Element article: articles)
    		{
    			String image = this.getImage(article);
    			String title = this.getTitle(article);
    	    	if (title.indexOf("Ten Pence Scores") != -1)	// There is one instance of this
    	    		continue;
    	    	String description = this.getDescription(article);
//    	    	String description = this.getFullDescriptionText(article);
//    	    	if (description.isEmpty())
//    	    		description = this.getDescription(article);
    	    	String readMore = this.getReadMoreLink(article);
    	    	String mp3 = this.getMP3Link(article);
    	    	episodes.add(new Episode(title, description, image, mp3, readMore, page.getDate()));
    		}
    	}



        // Create the PDF
        com.itextpdf.text.Document pdfDoc = new com.itextpdf.text.Document();
        pdfDoc.setPageSize(new Rectangle(PageSize.A4.getWidth(), PageSize.A4.getHeight()));
        PdfWriter.getInstance(pdfDoc, new FileOutputStream(PDF_NAME));
        pdfDoc.open();

        this.createCover(pdfDoc);
        this.createContents(pdfDoc, episodes);

//        for (int i = episodes.size() -1; i >= 0; i--)
        for (Episode episode: episodes)
        {
//        	Episode episode = episodes.get(i);
        	debugOut("Building PDF page for " + episode.getTitle());
            // The title
            Font titleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 20, BaseColor.BLACK);
            Chunk titleChunk = new Chunk(episode.getTitle() + "\n   ", titleFont);

            // The description
            Font descFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, BaseColor.BLACK);
            Chunk descChunk = new Chunk(episode.getDescription() /*+ "\n\n"*/, descFont);

            URL picUrl = new URL(episode.getImage());
            Image img = Image.getInstance(picUrl);

            // Scale image and centre it
            if (episode.getImage().equals("https://i0.wp.com/tenpencearcade.co.uk/wp-content/uploads/2014/12/10p-Star-Force-3.png") ||
            	episode.getImage().equals("https://i2.wp.com/tenpencearcade.co.uk/wp-content/uploads/2015/05/10p-lunar-rescue.png") ||
            	episode.getImage().equals("https://i0.wp.com/tenpencearcade.co.uk/wp-content/uploads/2016/05/10p-gunbird.jpg")
            )
            	img.scaleToFit((float)(PageSize.A4.getWidth() * LARGE_IMAGE_SCALE), (float)(PageSize.A4.getHeight() * LARGE_IMAGE_SCALE));
            else
            	img.scaleToFit((float)(PageSize.A4.getWidth() * IMAGE_SCALE), (float)(PageSize.A4.getHeight() * IMAGE_SCALE));
            float x = (PageSize.A4.getWidth() - img.getScaledWidth()) / 2;
            float y = (PageSize.A4.getHeight() - img.getScaledHeight()) / 2;
            img.setAbsolutePosition(x, y);

            // Create the episode link
            Font linkFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLUE);
            Chunk linkChunk = new Chunk("Click here for the podcast", linkFont);
            linkChunk.setAnchor(episode.getMp3());

            // Create the read more link
            Font readMoreFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLUE);
            Chunk readMoreChunk = new Chunk("Read More...", readMoreFont);
            readMoreChunk.setAnchor(episode.getReadMoreLink());

            pdfDoc.newPage();

            Paragraph titleParagraph = new Paragraph();
            titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            titleParagraph.add(titleChunk);

            Paragraph linkParagraph = new Paragraph();
            linkParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            linkParagraph.add(linkChunk);

            Paragraph descParagraph = new Paragraph();
            descParagraph.setAlignment(Paragraph.ALIGN_LEFT);
            descParagraph.add(descChunk);

            Paragraph readMoreParagraph = new Paragraph();
            readMoreParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            readMoreParagraph.add(readMoreChunk);


            pdfDoc.add(titleParagraph);
            pdfDoc.add(descParagraph);
            pdfDoc.add(readMoreParagraph);
            pdfDoc.add(img);
            pdfDoc.add(linkParagraph);
        }
        pdfDoc.close();
    }

    private void createCover(com.itextpdf.text.Document doc) throws DocumentException, MalformedURLException, IOException
    {
    	debugOut("Creating cover");
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 30, BaseColor.BLACK);
        Paragraph p = new Paragraph();
        p.setAlignment(Paragraph.ALIGN_CENTER);
        Chunk c = new Chunk("\n\n\nTen Pence Arcade Podcast\n\nEpisode Guide", f);
        p.add(c);
        doc.add(p);

        Image img = Image.getInstance(Episode.DEFAULT_IMAGE);
    	img.scaleToFit((float)(PageSize.A4.getWidth() * IMAGE_SCALE), (float)(PageSize.A4.getHeight() * IMAGE_SCALE));
	    float x = (PageSize.A4.getWidth() - img.getScaledWidth()) / 2;
	    float y = (PageSize.A4.getHeight() - img.getScaledHeight()) / 2;
	    img.setAbsolutePosition(x, y);
	    doc.add(img);
    }

    private void createContents(com.itextpdf.text.Document doc, List<Episode> episodes) throws DocumentException
    {
    	debugOut("Creating table of contents");
    	int currentItem = 1;
        List<Chunk> chunks = new ArrayList<Chunk>();
        Font contentFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, BaseColor.BLACK);
        doc.newPage();

        Font contentTitleFont = FontFactory.getFont(FontFactory.TIMES_ROMAN, 16, BaseColor.BLACK);
        Paragraph contentTitleParagrapoh = new Paragraph();
        contentTitleParagrapoh.setAlignment(Paragraph.ALIGN_CENTER);
        Chunk contentTitleChunk = new Chunk("List of Episodes as of " + this.getDateText() + "\n\n", contentTitleFont);
        contentTitleParagrapoh.add(contentTitleChunk);
        doc.add(contentTitleParagrapoh);


        boolean blockWritten = false;
        for (Episode e: episodes)
//        for (int i = episodes.size() -1; i >= 0; i--)
        {
//            Chunk chunk = new Chunk(episodes.get(i).getTitle(), contentFont);
            Chunk chunk = new Chunk(e.getTitle() + "  (" + e.getDateStamp() + ")", contentFont);
            chunks.add(chunk);
            currentItem++;
            blockWritten = false;
//        	if (currentItem > NUM_CONTENT_ITEMS_PER_PAGE || i == 0)
            if (currentItem > NUM_CONTENT_ITEMS_PER_PAGE)
        	{
        		for (Chunk c: chunks)
        		{
        			Paragraph p = new Paragraph();
        			p.add(c);
        			doc.add(p);
        		}
        		currentItem = 1;
        		doc.newPage();
        		chunks.clear();
        		blockWritten = true;
        	}
        }
        if (!blockWritten)
        {
    		for (Chunk c: chunks)
    		{
    			Paragraph p = new Paragraph();
    			p.add(c);
    			doc.add(p);
    		}
        }
    }

    private Element getTagAndClassAndChildren(Elements elements, String tagName, String className)
    {
        for (Element e: elements)
        {
            if (e.tagName().equals(tagName))
            {
                if (e.className().equals(className))
                    return e;
            }
            Element el = this.getTagAndClassAndChildren(e.children(), tagName, className);
            if (el != null)
                return el;
        }
        return null;
    }

    private Element getTagAndIdAndChildren(Elements elements, String tagName, String className)
    {
        for (Element e: elements)
        {
            if (e.tagName().equals(tagName))
            {
                if (e.id().equals(className))
                    return e;
            }
            Element el = this.getTagAndIdAndChildren(e.children(), tagName, className);
            if (el != null)
                return el;
        }
        return null;
    }

    private String getHtml(String url) throws IOException
    {
        return Jsoup.connect(url).get().html();
    }
}
