package org.chromium.aapt;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import android.util.Log;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;


public class XMLNode extends Chunk {
	public int OK=0;
	public int NO_ERROR=0;
	public int UNKNOWN_ERROR=-9999;
	String RESOURCES_ROOT_NAMESPACE = "http://schemas.android.com/apk/res/";
	String RESOURCES_ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android";
	String RESOURCES_AUTO_PACKAGE_NAMESPACE = "http://schemas.android.com/apk/res-auto";
	String RESOURCES_ROOT_PRV_NAMESPACE = "http://schemas.android.com/apk/prv/res/";

    String RESOURCES_PREFIX = RESOURCES_ROOT_NAMESPACE;
    String RESOURCES_PREFIX_AUTO_PACKAGE = RESOURCES_AUTO_PACKAGE_NAMESPACE;
    String RESOURCES_PRV_PREFIX = RESOURCES_ROOT_PRV_NAMESPACE;
    String RESOURCES_TOOLS_NAMESPACE = "http://schemas.android.com/tools";
    String XLIFF_XMLNS = "urn:oasis:names:tc:xliff:document:1.2";

    static int TYPE_NAMESPACE=0;
    static int TYPE_ELEMENT=1;
    static int TYPE_CDATA=2;
    
    String ALLOWED_XLIFF_ELEMENTS[] = {
        "bpt",
        "ept",
        "it",
        "ph",
        "g",
        "bx",
        "ex",
        "x"
    };

	private Vector<XMLNode> mChildren;
    private Vector<attribute_entry> mAttributes;
    private String mChars;
    private Hashtable<Integer, Integer> mAttributeOrder = new Hashtable<Integer, Integer>();
    
    String mNamespacePrefix;
    String mNamespaceUri;
    String mElementName;
    String mFilename=null;
    String mComment=null;
    int mStartLineNumber=0;
    int mEndLineNumber=0;
    Value mCharsValue;
    int mNextAttributeIndex;
    
    private String LOG_TAG = "APKPackage";
    
    class ParseState
    {
        String filename;
        XMLNode  root;
        Vector<XMLNode> stack;
        String pendingComment;
    };
    class attribute_entry {
        String ns;
        String name;
        String string;
        Value value;
        int index;
        int nameResId;
        int namePoolIdx;
        
        attribute_entry()
        {
            value.dataType = Value.TYPE_NULL;
        }

        boolean needStringValue()  {
            return nameResId == 0
                || value.dataType == Value.TYPE_NULL
                || value.dataType == Value.TYPE_STRING;
        }
        
    };

    public XMLNode(String filename) {
    	mChildren = new Vector<XMLNode>();
        mFilename=filename;
    }

    public XMLNode(String filename, String s1, String s2, boolean isNamespace) {
    	mChildren = new Vector<XMLNode>();
        mFilename=filename;
        if (isNamespace) {
            mNamespacePrefix = s1;
            mNamespaceUri = s2;
        } else {
            mNamespaceUri = s1;
            mElementName =s2;
        }
    }

    public static XMLNode newNamespace(String filename, String prefix, String uri) {
        return new XMLNode(filename, prefix, uri, true);
    }
    
    public static XMLNode newElement( String filename, String ns, String name) {
        return new XMLNode(filename, ns, name, false);
    }
    
    public static XMLNode newCData(String filename) {
        return new XMLNode(filename);
    }

    public void parse(File xmlFile)  throws Exception {
    	XMLNode root = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new FileReader(xmlFile));
            int eventType = -1;
            while ( eventType != XmlPullParser.END_DOCUMENT) {
                eventType = parser.getEventType();
                if (eventType == XmlPullParser.START_TAG) {
                	startElement(parser);
                } else if(eventType == XmlPullParser.TEXT) {
                	textElement(parser);
                } else if(eventType == XmlPullParser.END_TAG) {
                	endElement(root, parser);
                }
                eventType = parser.next();			
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
    private void textElement(XmlPullParser parser ){
        XMLNode x = new XMLNode(mFilename);
        x.mElementName=parser.getName();
    	x.mChars=parser.getText();
    }
    private void endElement(XMLNode current, XmlPullParser parser ) throws Exception{
        if(current.mElementName != parser.getName()) {
        	throw new Exception("Invalid XML element end");
        }
        current.mEndLineNumber = parser.getLineNumber();
    }
    private void startElement(XmlPullParser parser ){
        XMLNode x = new XMLNode(mFilename);
        x.mElementName=parser.getName();
        x.mStartLineNumber=parser.getLineNumber();
        x.mNamespaceUri = parser.getNamespace();
        for(int i=0;i<parser.getAttributeCount();i++){
        	x.addAttribute(parser.getAttributeNamespace(i),parser.getAttributeName(i),parser.getAttributeValue(i));
        }
    }
    public int getType() {
        if (mElementName.length() != 0) {
            return TYPE_ELEMENT;
        }
        if (mNamespaceUri.length() != 0) {
            return TYPE_NAMESPACE;
        }
        return TYPE_CDATA;
    }

    public String getNamespacePrefix() {
        return mNamespacePrefix;
    }

    public String getNamespaceUri() {
        return mNamespaceUri;
    }

    public String getElementNamespace() {
        return mNamespaceUri;
    }

    public String getElementName() {
        return mElementName;
    }

    public Vector<XMLNode> getChildren() {
        return mChildren;
    }

    public String getFilename() {
        return mFilename;
    }
    
    public Vector<attribute_entry> getAttributes() {
        return mAttributes;
    }

    public attribute_entry getAttribute(String ns, String name) {
        for (int i=0; i<mAttributes.size(); i++) {
            attribute_entry  ae = mAttributes.elementAt(i);
            if (ae.ns == ns && ae.name == name) {
                return ae;
            }
        }
        return null;
    }

    public attribute_entry editAttribute(String ns,  String name) {
        for (int i=0; i<mAttributes.size(); i++) {
            attribute_entry ae = mAttributes.elementAt(i);
            if (ae.ns == ns && ae.name == name) {
                return ae;
            }
        }
        return null;
    }

    public String getCData() {
        return mChars;
    }

    public String getComment() {
        return mComment;
    }

    public int getStartLineNumber() {
        return mStartLineNumber;
    }

    public int getEndLineNumber() {
        return mEndLineNumber;
    }


    public XMLNode searchElement(String tagNamespace, String tagName) {
        if (getType() == XMLNode.TYPE_ELEMENT
            && mNamespaceUri == tagNamespace
            && mElementName == tagName) {
            return this;
        }
    
        for (int i=0; i<mChildren.size(); i++) {
            XMLNode found = mChildren.elementAt(i).searchElement(tagNamespace, tagName);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    XMLNode getChildElement(String tagNamespace, String tagName) {
        for (int i=0; i<mChildren.size(); i++) {
            XMLNode child = mChildren.elementAt(i);
            if (child.getType() == XMLNode.TYPE_ELEMENT
                   && child.mNamespaceUri == tagNamespace
                   && child.mElementName == tagName) {
               return child;
            }
        }
        return null;
    }
    void SourcePosError(String filename, int lineno, String msg) {
    	Log.e(LOG_TAG, filename+"["+lineno+"] "+msg);
    }
    int addChild(XMLNode child) {
        if (getType() == XMLNode.TYPE_CDATA) {
            SourcePosError(mFilename, child.getStartLineNumber(),"Child to CDATA node.");
            return UNKNOWN_ERROR;
        }
        mChildren.add(child);
        return NO_ERROR;
    }

    int insertChildAt(XMLNode child, int index) {
        if (getType() == XMLNode.TYPE_CDATA) {
            SourcePosError(mFilename, child.getStartLineNumber(),"Child to CDATA node.");
            return UNKNOWN_ERROR;
        }
        mChildren.insertElementAt(child, index);
        return NO_ERROR;
    }

    int addAttribute(String ns, String name, String value) {
        if (getType() == XMLNode.TYPE_CDATA) {
            SourcePosError(mFilename, getStartLineNumber(),"Child to CDATA node.");
           return UNKNOWN_ERROR;
        }

        if (ns != RESOURCES_TOOLS_NAMESPACE) {
            attribute_entry e = new attribute_entry();
            e.index = mNextAttributeIndex++;
            e.ns = ns;
            e.name = name;
            e.string = value;
            mAttributes.add(e);
            mAttributeOrder.put(e.index, mAttributes.size()-1);
        }
        return NO_ERROR;
    }

    void setAttributeResID(int attrIdx, int resId) {
        attribute_entry  e = mAttributes.elementAt(attrIdx);
        if (e.nameResId !=0) {
            mAttributeOrder.remove(e.nameResId);
        } else {
            mAttributeOrder.remove(e.index);
        }
        mAttributes.elementAt(attrIdx).nameResId = resId;
        mAttributeOrder.put(resId, attrIdx);
    }

    int appendChars( String chars){
        if (getType() != XMLNode.TYPE_CDATA) {
            SourcePosError(mFilename, getStartLineNumber(),"Adding characters to element node.");
            return UNKNOWN_ERROR;
        }
        mChars.concat(chars);
        return NO_ERROR;
    }

    int appendComment(String comment) {
        if (mComment.length() > 0) {
            mComment.concat("\n");
        }
        mComment.concat(comment);
        return NO_ERROR;
    }

    void setStartLineNumber(int line) {
        mStartLineNumber = line;
    }

    void setEndLineNumber(int line) {
        mEndLineNumber = line;
    }

    void removeWhitespace(boolean stripAll, Vector<String> cDataTags) {
        if (!cDataTags.isEmpty()) {
            String tag = mElementName;
            for( String ctag : cDataTags) {
                if (tag == ctag) {
                    stripAll = false;
                    break;
                }
            }
        }
        for (int i=0; i< mChildren.size(); i++) {
            XMLNode node = mChildren.elementAt(i);
            if (node.getType() == XMLNode.TYPE_CDATA) {
                // This is a CDATA node...
                String p = node.mChars.trim();
                if (p.isEmpty()) {
                    if (stripAll) {
                       // Remove this node!
                       mChildren.removeElementAt(i);
                       i--;
                    } else {
                        node.mChars = " ";
                    }
                }
            } else {
                node.removeWhitespace(stripAll, cDataTags);
            }
        }
    }

    int parseValues(AaptAssets assets, ResourceTable table) {
        boolean hasErrors = false;
    
        if (getType() == TYPE_ELEMENT) {
            String defPackage = assets.getPackage();
            for(int i=0; i<mAttributes.size(); i++) {
                attribute_entry e = mAttributes.elementAt(i);
                AccessorCookie ac = new AccessorCookie(new SourcePos(mFilename, getStartLineNumber()), e.name,  e.string);
//                table->setCurrentXmlPos(SourcePos(mFilename, getStartLineNumber()));
                ResTable.ResourceValue b=assets.getIncludedResources()
                        .stringToValue(e.string, true, true,
                                e.nameResId, null, defPackage, table, ac,0);
                e.value=b.outValue;
                e.string=b.outString;
                if (!b.ok) {
                    hasErrors = true;
                }
            }
        }

        for (int i=0; i<mChildren.size(); i++) {
            int err = mChildren.elementAt(i).parseValues(assets, table);
            if (err != NO_ERROR) {
                hasErrors = true;
            }
        }
        return hasErrors ? UNKNOWN_ERROR : NO_ERROR;
    }

    int assignResourceIds( AaptAssets assets, ResourceTable table) {
        boolean hasErrors = false;
    
        if (getType() == TYPE_ELEMENT) {
            String attr ="attr";
            String errorMsg;
            for (int i=0; i<mAttributes.size(); i++) {
                attribute_entry e = mAttributes.elementAt(i);
                if (e.ns.length() <= 0) continue;
                boolean nsIsPublic;
                String  pkg = getNamespaceResourcePackage( assets.getPackage() , e.ns,  nsIsPublic);
                if (pkg.length() <= 0) continue;
                int res = table != null
                    ? table.getResId(e.name, attr, pkg, errorMsg, nsIsPublic)
                    : assets.getIncludedResources().
                        identifierForName(e.name, e.name.length(),
                                      attr, attr.length(),
                                      pkg, pkg.length());
                if (res != 0) {
                    setAttributeResID(i, res);
                } else {
                    SourcePosError(mFilename, getStartLineNumber(),
                        "No resource identifier found for attribute '%s' in package '%s'\n"+ e.name+pkg);
                    hasErrors = true;
                }
            }
        }
        int N = mChildren.size();
        for (int i=0; i<N; i++) {
            int err = mChildren.elementAt(i).assignResourceIds(assets, table);
            if (err < NO_ERROR) {
                hasErrors = true;
            }
        }
        return hasErrors ? UNKNOWN_ERROR : NO_ERROR;
    }
/*
    int flatten( AaptFile dest, boolean stripComments, boolean stripRawValues) {
	    StringPool strings = new StringPool() ;
	    Vector<Integer> resids;
	    
	    // First collect just the strings for attribute names that have a
	    // resource ID assigned to them.  This ensures that the resource ID
	    // array is compact, and makes it easier to deal with attribute names
	    // in different namespaces (and thus with different resource IDs).
	    collect_resid_strings(strings, resids);
	
	    // Next collect all remaining strings.
	    collect_strings(strings, resids, stripComments, stripRawValues);
	
	    sp<AaptFile> stringPool = strings.createStringBlock();
	
	    ResXMLTree_header header;
	    memset(&header, 0, sizeof(header));
	    header.header.type = htods(RES_XML_TYPE);
	    header.header.headerSize = htods(sizeof(header));
	
	    const size_t basePos = dest->getSize();
	    dest->writeData(&header, sizeof(header));
	    dest->writeData(stringPool->getData(), stringPool->getSize());
	
	    // If we have resource IDs, write them.
	    if (resids.size() > 0) {
	        const size_t resIdsPos = dest->getSize();
	        const size_t resIdsSize =
	            sizeof(ResChunk_header)+(sizeof(uint32_t)*resids.size());
	        ResChunk_header* idsHeader = (ResChunk_header*)
	            (((const uint8_t*)dest->editData(resIdsPos+resIdsSize))+resIdsPos);
	        idsHeader->type = htods(RES_XML_RESOURCE_MAP_TYPE);
	        idsHeader->headerSize = htods(sizeof(*idsHeader));
	        idsHeader->size = htodl(resIdsSize);
	        uint32_t* ids = (uint32_t*)(idsHeader+1);
	        for (size_t i=0; i<resids.size(); i++) {
	            *ids++ = htodl(resids[i]);
	        }
	    }
	
	    flatten_node(strings, dest, stripComments, stripRawValues);
	
	    void* data = dest->editData();
	    ResXMLTree_header* hd = (ResXMLTree_header*)(((uint8_t*)data)+basePos);
	    size_t size = dest->getSize()-basePos;
	    hd->header.size = htodl(dest->getSize()-basePos);
	
	    NOISY(aout << "XML resource:"
	          << HexDump(dest->getData(), dest->getSize()) << endl);
	
	    #if PRINT_STRING_METRICS
	    fprintf(stderr, "**** total xml size: %d / %d%% strings (in %s)\n",
	        dest->getSize(), (stringPool->getSize()*100)/dest->getSize(),
	        dest->getPath().string());
	    #endif
	        
	    return NO_ERROR;
	}

    static void splitName(String name, String outNs, String outName) {
	    const char* p = name;
	    while (*p != 0 && *p != 1) {
	        p++;
	    }
	    if (*p == 0) {
	        *outNs = String16();
	        *outName = String16(name);
	    } else {
	        *outNs = String16(name, (p-name));
	        *outName = String16(p+1);
	    }
	}
*/
/*
void XMLCALL
XMLNode::startNamespace(void *userData, const char *prefix, const char *uri)
{
    ParseState* st = (ParseState*)userData;
    sp<XMLNode> node = XMLNode::newNamespace(st->filename, 
            String16(prefix != NULL ? prefix : ""), String16(uri));
    node->setStartLineNumber(XML_GetCurrentLineNumber(st->parser));
    if (st->stack.size() > 0) {
        st->stack.itemAt(st->stack.size()-1)->addChild(node);
    } else {
        st->root = node;
    }
    st->stack.push(node);
}
*/

    /*void XMLCALL
XMLNode::startElement(void *userData, const char *name, const char **atts)
{
    NOISY_PARSE(printf("Start Element: %s\n", name));
    ParseState st = (ParseState*)userData;
    String16 ns16, name16;
    splitName(name, &ns16, &name16);
    sp<XMLNode> node = XMLNode::newElement(st->filename, ns16, name16);
    node->setStartLineNumber(XML_GetCurrentLineNumber(st->parser));
    if (st->pendingComment.size() > 0) {
        node->appendComment(st->pendingComment);
        st->pendingComment = String16();
    }
    if (st->stack.size() > 0) {
        st->stack.itemAt(st->stack.size()-1)->addChild(node);
    } else {
        st->root = node;
    }
    st->stack.push(node);

    for (int i = 0; atts[i]; i += 2) {
        splitName(atts[i], &ns16, &name16);
        node->addAttribute(ns16, name16, String16(atts[i+1]));
    }
}

void XMLCALL
XMLNode::characterData(void *userData, const XML_Char *s, int len)
{
    NOISY_PARSE(printf("CDATA: \"%s\"\n", String8(s, len).string()));
    ParseState* st = (ParseState*)userData;
    sp<XMLNode> node = NULL;
    if (st->stack.size() == 0) {
        return;
    }
    sp<XMLNode> parent = st->stack.itemAt(st->stack.size()-1);
    if (parent != NULL && parent->getChildren().size() > 0) {
        node = parent->getChildren()[parent->getChildren().size()-1];
        if (node->getType() != TYPE_CDATA) {
            // Last node is not CDATA, need to make a new node.
            node = NULL;
        }
    }

    if (node == NULL) {
        node = XMLNode::newCData(st->filename);
        node->setStartLineNumber(XML_GetCurrentLineNumber(st->parser));
        parent->addChild(node);
    }

    node->appendChars(String16(s, len));
}

void XMLCALL
XMLNode::endElement(void *userData, const char *name)
{
    NOISY_PARSE(printf("End Element: %s\n", name));
    ParseState* st = (ParseState*)userData;
    sp<XMLNode> node = st->stack.itemAt(st->stack.size()-1);
    node->setEndLineNumber(XML_GetCurrentLineNumber(st->parser));
    if (st->pendingComment.size() > 0) {
        node->appendComment(st->pendingComment);
        st->pendingComment = String16();
    }
    String16 ns16, name16;
    splitName(name, &ns16, &name16);
    LOG_ALWAYS_FATAL_IF(node->getElementNamespace() != ns16
                        || node->getElementName() != name16,
                        "Bad end element %s", name);
    st->stack.pop();
}
*/
/*
void XMLCALL
XMLNode::endNamespace(void *userData, const char *prefix)
{
    const char* nonNullPrefix = prefix != NULL ? prefix : "";
    NOISY_PARSE(printf("End Namespace: %s\n", prefix));
    ParseState* st = (ParseState*)userData;
    sp<XMLNode> node = st->stack.itemAt(st->stack.size()-1);
    node->setEndLineNumber(XML_GetCurrentLineNumber(st->parser));
    LOG_ALWAYS_FATAL_IF(node->getNamespacePrefix() != String16(nonNullPrefix),
                        "Bad end namespace %s", prefix);
    st->stack.pop();
}

void XMLCALL
XMLNode::commentData(void *userData, const char *comment)
{
    NOISY_PARSE(printf("Comment: %s\n", comment));
    ParseState* st = (ParseState*)userData;
    if (st->pendingComment.size() > 0) {
        st->pendingComment.append(String16("\n"));
    }
    st->pendingComment.append(String16(comment));
}
*/
    int  collect_strings(StringPool dest, Vector<Integer> outResIds, boolean stripComments, boolean stripRawValues) {
    collect_attr_strings(dest, outResIds, true);
    
    int i;
    if (RESOURCES_TOOLS_NAMESPACE != mNamespaceUri) {
        if (mNamespacePrefix.length() > 0) {
            dest.addString(mNamespacePrefix, true);
        }
        if (mNamespaceUri.length() > 0) {
            dest.addString(mNamespaceUri, true);
        }
    }
    if (mElementName.length() > 0) {
        dest.addString(mElementName, true);
    }

    if (!stripComments && mComment.length() > 0) {
        dest.addString(mComment, true);
    }

    int NA = mAttributes.size();

    for (i=0; i<NA; i++) {
        attribute_entry  ae = mAttributes.elementAt(i);
        if (ae.ns.length() > 0) {
            dest.addString(ae.ns, true);
        }
        if (!stripRawValues || ae.needStringValue()) {
            dest.addString(ae.string, true);
        }
    }

    if (mElementName.length() == 0) {
        // If not an element, include the CDATA, even if it is empty.
        dest.addString(mChars, true);
    }

    int NC = mChildren.size();

    for (i=0; i<NC; i++) {
        mChildren.elementAt(i).collect_strings(dest, outResIds, stripComments, stripRawValues);
    }

    return NO_ERROR;
}

    int  collect_attr_strings(StringPool outPool, Vector<Integer> outResIds, boolean allAttrs) {
        int NA = mAttributes.size();

	    for (int i=0; i<NA; i++) {
	        attribute_entry attr = mAttributes.itemAt(i);
	        int id = attr.nameResId;
	        if (id || allAttrs) {
	            // See if we have already assigned this resource ID to a pooled
	            // string...
	            Vector<Integer> indices = outPool.offsetsForString(attr.name);
	            int idx = -1;
	            if (indices != NULL) {
	                int NJ = indices.size();
	                int NR = outResIds.size();
	                for (int j=0; j<NJ; j++) {
	                    int strIdx = indices.elementAt(j);
	                    if (strIdx >= NR) {
	                        if (id == 0) {
	                            // We don't need to assign a resource ID for this one.
	                            idx = strIdx;
	                            break;
	                        }
	                        // Just ignore strings that are out of range of
	                        // the currently assigned resource IDs...  we add
	                        // strings as we assign the first ID.
	                    } else if (outResIds.elementAt(strIdx) == id) {
	                        idx = strIdx;
	                        break;
	                    }
	                }
	            }
	            if (idx < 0) {
	                idx = outPool.add(attr.name);
	                if (id != 0) {
	                    while (outResIds.size() <= idx) {
	                        outResIds->add(0);
	                    }
	                    outResIds->replaceAt(id, idx);
	                }
	            }
	            attr.namePoolIdx = idx;
	            NOISY(printf("String %s offset=0x%08x\n",
	                         String8(attr.name).string(), idx));
	        }
	    }
	
	    return NO_ERROR;
}

int collect_resid_strings(StringPool outPool, Vector<Integer> outResIds){
    collect_attr_strings(outPool, outResIds, false);

    int NC = mChildren.size();

    for (int i=0; i<NC; i++) {
        mChildren.elementAt(i).collect_resid_strings(outPool, outResIds);
    }

    return NO_ERROR;
}
/*
int  flatten_node(StringPool  strings, AaptFile  dest,  boolean stripComments, bool stripRawValues) {
    ResXMLTree_node node;
    ResXMLTree_cdataExt cdataExt;
    ResXMLTree_namespaceExt namespaceExt;
    ResXMLTree_attrExt attrExt;
    const void* extData = NULL;
    size_t extSize = 0;
    ResXMLTree_attribute attr;
    bool writeCurrentNode = true;

    const size_t NA = mAttributes.size();
    const size_t NC = mChildren.size();
    size_t i;

    LOG_ALWAYS_FATAL_IF(NA != mAttributeOrder.size(), "Attributes messed up!");

    const String16 id16("id");
    const String16 class16("class");
    const String16 style16("style");

    const type type = getType();

    memset(&node, 0, sizeof(node));
    memset(&attr, 0, sizeof(attr));
    node.header.headerSize = htods(sizeof(node));
    node.lineNumber = htodl(getStartLineNumber());
    if (!stripComments) {
        node.comment.index = htodl(
            mComment.size() > 0 ? strings.offsetForString(mComment) : -1);
        //if (mComment.size() > 0) {
        //  printf("Flattening comment: %s\n", String8(mComment).string());
        //}
    } else {
        node.comment.index = htodl((uint32_t)-1);
    }
    if (type == TYPE_ELEMENT) {
        node.header.type = htods(RES_XML_START_ELEMENT_TYPE);
        extData = &attrExt;
        extSize = sizeof(attrExt);
        memset(&attrExt, 0, sizeof(attrExt));
        if (mNamespaceUri.size() > 0) {
            attrExt.ns.index = htodl(strings.offsetForString(mNamespaceUri));
        } else {
            attrExt.ns.index = htodl((uint32_t)-1);
        }
        attrExt.name.index = htodl(strings.offsetForString(mElementName));
        attrExt.attributeStart = htods(sizeof(attrExt));
        attrExt.attributeSize = htods(sizeof(attr));
        attrExt.attributeCount = htods(NA);
        attrExt.idIndex = htods(0);
        attrExt.classIndex = htods(0);
        attrExt.styleIndex = htods(0);
        for (i=0; i<NA; i++) {
            ssize_t idx = mAttributeOrder.valueAt(i);
            const attribute_entry& ae = mAttributes.itemAt(idx);
            if (ae.ns.size() == 0) {
                if (ae.name == id16) {
                    attrExt.idIndex = htods(i+1);
                } else if (ae.name == class16) {
                    attrExt.classIndex = htods(i+1);
                } else if (ae.name == style16) {
                    attrExt.styleIndex = htods(i+1);
                }
            }
        }
    } else if (type == TYPE_NAMESPACE) {
        if (mNamespaceUri == RESOURCES_TOOLS_NAMESPACE) {
            writeCurrentNode = false;
        } else {
            node.header.type = htods(RES_XML_START_NAMESPACE_TYPE);
            extData = &namespaceExt;
            extSize = sizeof(namespaceExt);
            memset(&namespaceExt, 0, sizeof(namespaceExt));
            if (mNamespacePrefix.size() > 0) {
                namespaceExt.prefix.index = htodl(strings.offsetForString(mNamespacePrefix));
            } else {
                namespaceExt.prefix.index = htodl((uint32_t)-1);
            }
            namespaceExt.prefix.index = htodl(strings.offsetForString(mNamespacePrefix));
            namespaceExt.uri.index = htodl(strings.offsetForString(mNamespaceUri));
        }
        LOG_ALWAYS_FATAL_IF(NA != 0, "Namespace nodes can't have attributes!");
    } else if (type == TYPE_CDATA) {
        node.header.type = htods(RES_XML_CDATA_TYPE);
        extData = &cdataExt;
        extSize = sizeof(cdataExt);
        memset(&cdataExt, 0, sizeof(cdataExt));
        cdataExt.data.index = htodl(strings.offsetForString(mChars));
        cdataExt.typedData.size = htods(sizeof(cdataExt.typedData));
        cdataExt.typedData.res0 = 0;
        cdataExt.typedData.dataType = mCharsValue.dataType;
        cdataExt.typedData.data = htodl(mCharsValue.data);
        LOG_ALWAYS_FATAL_IF(NA != 0, "CDATA nodes can't have attributes!");
    }

    node.header.size = htodl(sizeof(node) + extSize + (sizeof(attr)*NA));

    if (writeCurrentNode) {
        dest->writeData(&node, sizeof(node));
        if (extSize > 0) {
            dest->writeData(extData, extSize);
        }
    }

    for (i=0; i<NA; i++) {
        ssize_t idx = mAttributeOrder.valueAt(i);
        const attribute_entry& ae = mAttributes.itemAt(idx);
        if (ae.ns.size() > 0) {
            attr.ns.index = htodl(strings.offsetForString(ae.ns));
        } else {
            attr.ns.index = htodl((uint32_t)-1);
        }
        attr.name.index = htodl(ae.namePoolIdx);

        if (!stripRawValues || ae.needStringValue()) {
            attr.rawValue.index = htodl(strings.offsetForString(ae.string));
        } else {
            attr.rawValue.index = htodl((uint32_t)-1);
        }
        attr.typedValue.size = htods(sizeof(attr.typedValue));
        if (ae.value.dataType == Value.TYPE_NULL
                || ae.value.dataType == Value.TYPE_STRING) {
            attr.typedValue.res0 = 0;
            attr.typedValue.dataType = Value.TYPE_STRING;
            attr.typedValue.data = htodl(strings.offsetForString(ae.string));
        } else {
            attr.typedValue.res0 = 0;
            attr.typedValue.dataType = ae.value.dataType;
            attr.typedValue.data = htodl(ae.value.data);
        }
        dest->writeData(&attr, sizeof(attr));
    }

    for (i=0; i<NC; i++) {
        status_t err = mChildren.itemAt(i)->flatten_node(strings, dest,
                stripComments, stripRawValues);
        if (err != NO_ERROR) {
            return err;
        }
    }

    if (type == TYPE_ELEMENT) {
        ResXMLTree_endElementExt endElementExt;
        memset(&endElementExt, 0, sizeof(endElementExt));
        node.header.type = htods(RES_XML_END_ELEMENT_TYPE);
        node.header.size = htodl(sizeof(node)+sizeof(endElementExt));
        node.lineNumber = htodl(getEndLineNumber());
        node.comment.index = htodl((uint32_t)-1);
        endElementExt.ns.index = attrExt.ns.index;
        endElementExt.name.index = attrExt.name.index;
        dest->writeData(&node, sizeof(node));
        dest->writeData(&endElementExt, sizeof(endElementExt));
    } else if (type == TYPE_NAMESPACE) {
        if (writeCurrentNode) {
            node.header.type = htods(RES_XML_END_NAMESPACE_TYPE);
            node.lineNumber = htodl(getEndLineNumber());
            node.comment.index = htodl((uint32_t)-1);
            node.header.size = htodl(sizeof(node)+extSize);
            dest->writeData(&node, sizeof(node));
            dest->writeData(extData, extSize);
        }
    }

    return NO_ERROR;
}
*/
	@Override
	public int getChunkType() {
		return RES_XML_TYPE;
	}

	@Override
	public int getHeaderSize() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeHeader(OutputStream output) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int computeSize() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void write(OutputStream output) throws IOException {
		// TODO Auto-generated method stub
		
	}

}

