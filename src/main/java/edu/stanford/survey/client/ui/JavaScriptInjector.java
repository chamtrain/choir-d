package edu.stanford.survey.client.ui;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadElement;
import com.google.gwt.dom.client.ScriptElement;

public class JavaScriptInjector {
 
     /**
     * Injects the JavaScript code into a
     * {@code <script type="text/javascript">...</script>} 
     * element in the document header.
     *
     * @param javascript is the JavaScript code
     */
    public static void injectHeadScript(String javascript) {
        HeadElement head = getHead();
        ScriptElement element = createScriptElement();
        element.setText(javascript);
        head.appendChild(element);
    }

    /**
     * Injects the JavaScript url
     * {@code <script type="text/javascript">...</script>} 
     * element in the document header.
     *
     * @param javascript is the JavaScript code
     */
    public static String injectHeadUrl(String urlString) {
      HeadElement head = getHead();
      ScriptElement element = createScriptElement();
      element.setSrc(urlString);
      head.appendChild(element);
      return element.toString();
    }
    
    /**
     * Injects the JavaScript code into a
     * {@code <script type="text/javascript">...</script>} 
     * element in the document body.
     *
     * @param javascript is the JavaScript code
     */
    public static void injectBodyScript(String javascript) {
        BodyElement body = getBody();
        ScriptElement element = createScriptElement();
        element.setText(javascript);
        body.appendChild(element);
    }
    
    /**
     * Injects the JavaScript url
     * {@code <script type="text/javascript">...</script>} 
     * element in the document body.
    
     * @param urlString
     */
    public static void injectBodyURL(String urlString) {
      BodyElement body = getBody();
      ScriptElement element = createScriptElement();
      element.setSrc(urlString);
      body.appendChild(element);
  }
    
    private static ScriptElement createScriptElement() {
      ScriptElement script = Document.get().createScriptElement();
      script.setAttribute("type", "text/javascript");
      script.setAttribute("charset", "UTF-8");
      return script;
  }

  private static HeadElement getHead() {
      Element element = Document.get().getElementsByTagName("head")
              .getItem(0);
      assert element != null : "HTML Head element required";
      return  HeadElement.as(element);
  }
  
  private static BodyElement getBody() {
    Element element = Document.get().getElementsByTagName("body")
            .getItem(0);
    assert element != null : "HTML body element required";
    return  BodyElement.as(element);
  }

}
