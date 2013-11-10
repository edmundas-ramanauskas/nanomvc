package org.nanomvc.mvc;

import org.nanomvc.mvc.Controller;

public class DefaultCotroller extends Controller
{
  public void doIndex()
  {
    output("This is default controller.");
  }
}