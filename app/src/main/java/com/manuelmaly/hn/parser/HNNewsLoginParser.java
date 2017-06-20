package com.manuelmaly.hn.parser;

import com.manuelmaly.hn.model.HNPost;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Returns the hidden FNID form parameter returned by the HN login page.
 * @author manuelmaly
 *
 */
public class HNNewsLoginParser extends BaseHTMLParser<HNPost> {

    @Override
    public HNPost parseDocument(Element doc) throws Exception {
        if (doc == null)
            return null;

        Elements hiddenInput = doc.select("input[type=hidden]");
        if (hiddenInput.size() == 0)
            return null;
        return null;
        //return hiddenInput.get(0).attr("value");
    }

}
