package com.manuelmaly.hn.util;

import java.util.List;

/**
 * Created by TPS on 6/26/2017.
 */

public class HtmlUtil
{

    private static final String HIDE_HEADER_STYLE = "<style>div.headline{display:none;}</style>";

    private static final String NEEDED_FORMAT_CSS_TAG = "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\"/>";

    private static final String NEEDED_FORMAT_JS_TAG = "<script src=\"%s\"></script>";

    public static final String MIME_TYPE = "text/html; charset=utf-8";

    public static final String ENCODING = "utf-8";

    private HtmlUtil()
    {

    }

    public static String createCssTag(String url)
    {

        return String.format(NEEDED_FORMAT_CSS_TAG, url);
    }

    public static String createCssTag(List<String> urls)
    {

        final StringBuilder sb = new StringBuilder();
        for (String url : urls)
        {
            sb.append(createCssTag(url));
        }
        return sb.toString();
    }

    public static String createJsTag(String url)
    {

        return String.format(NEEDED_FORMAT_JS_TAG, url);
    }

    public static String createJsTag(List<String> urls)
    {

        final StringBuilder sb = new StringBuilder();
        for (String url : urls)
        {
            sb.append(createJsTag(url));
        }
        return sb.toString();
    }

    public static String createHtmlData(String html, List<String> cssList, List<String> jsList)
    {
        final String css = HtmlUtil.createCssTag(cssList);
        final String js = HtmlUtil.createJsTag(jsList);
        return css.concat(HIDE_HEADER_STYLE).concat(html).concat(js);
    }
}

