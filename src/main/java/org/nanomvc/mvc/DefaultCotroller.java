package org.nanomvc.mvc;

import org.nanomvc.mvc.Controller;

public class DefaultCotroller extends Controller
{
    public Result doIndex() {
        return text("This is default controller.");
    }
}