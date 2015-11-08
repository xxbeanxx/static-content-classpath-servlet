package com.github.xxbeanxx.servlet.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Greg Baker
 */
public class ResourceUtils {

	private static final int DEFAULT_BUFFER_SIZE = 4096;
	
	private static final int EOF = -1;

	private static final Map<String, String> MIME_TYPE_TO_EXTENSION_MAP = new HashMap<String, String>();

	private static final Map<String, String> EXTENSION_TO_MIME_TYPE_MAP = new HashMap<String, String>();

	static {
		// The following table is based on /etc/mime.types
		//
		// Note that this list is _not_ in alphabetical order and must not be
		// sorted. The "most popular" extension must come first, so that it's
		// the one returned by guessExtensionFromMimeType.

		addMimeType("application/andrew-inset", "ez");
		addMimeType("application/dsptype", "tsp");
		addMimeType("application/futuresplash", "spl");
		addMimeType("application/hta", "hta");
		addMimeType("application/mac-binhex40", "hqx");
		addMimeType("application/mac-compactpro", "cpt");
		addMimeType("application/mathematica", "nb");
		addMimeType("application/msaccess", "mdb");
		addMimeType("application/oda", "oda");
		addMimeType("application/ogg", "ogg");
		addMimeType("application/pdf", "pdf");
		addMimeType("application/pgp-keys", "key");
		addMimeType("application/pgp-signature", "pgp");
		addMimeType("application/pics-rules", "prf");
		addMimeType("application/rar", "rar");
		addMimeType("application/rdf+xml", "rdf");
		addMimeType("application/rss+xml", "rss");
		addMimeType("application/zip", "zip");
		addMimeType("application/vnd.android.package-archive", "apk");
		addMimeType("application/vnd.cinderella", "cdy");
		addMimeType("application/vnd.ms-pki.stl", "stl");
		addMimeType("application/vnd.oasis.opendocument.database", "odb");
		addMimeType("application/vnd.oasis.opendocument.formula", "odf");
		addMimeType("application/vnd.oasis.opendocument.graphics", "odg");
		addMimeType("application/vnd.oasis.opendocument.graphics-template", "otg");
		addMimeType("application/vnd.oasis.opendocument.image", "odi");
		addMimeType("application/vnd.oasis.opendocument.spreadsheet", "ods");
		addMimeType("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
		addMimeType("application/vnd.oasis.opendocument.text", "odt");
		addMimeType("application/vnd.oasis.opendocument.text-master", "odm");
		addMimeType("application/vnd.oasis.opendocument.text-template", "ott");
		addMimeType("application/vnd.oasis.opendocument.text-web", "oth");
		addMimeType("application/vnd.google-earth.kml+xml", "kml");
		addMimeType("application/vnd.google-earth.kmz", "kmz");
		addMimeType("application/msword", "doc");
		addMimeType("application/msword", "dot");
		addMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
		addMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
		addMimeType("application/vnd.ms-excel", "xls");
		addMimeType("application/vnd.ms-excel", "xlt");
		addMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
		addMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
		addMimeType("application/vnd.ms-powerpoint", "ppt");
		addMimeType("application/vnd.ms-powerpoint", "pot");
		addMimeType("application/vnd.ms-powerpoint", "pps");
		addMimeType("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
		addMimeType("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
		addMimeType("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
		addMimeType("application/vnd.rim.cod", "cod");
		addMimeType("application/vnd.smaf", "mmf");
		addMimeType("application/vnd.stardivision.calc", "sdc");
		addMimeType("application/vnd.stardivision.draw", "sda");
		addMimeType("application/vnd.stardivision.impress", "sdd");
		addMimeType("application/vnd.stardivision.impress", "sdp");
		addMimeType("application/vnd.stardivision.math", "smf");
		addMimeType("application/vnd.stardivision.writer", "sdw");
		addMimeType("application/vnd.stardivision.writer", "vor");
		addMimeType("application/vnd.stardivision.writer-global", "sgl");
		addMimeType("application/vnd.sun.xml.calc", "sxc");
		addMimeType("application/vnd.sun.xml.calc.template", "stc");
		addMimeType("application/vnd.sun.xml.draw", "sxd");
		addMimeType("application/vnd.sun.xml.draw.template", "std");
		addMimeType("application/vnd.sun.xml.impress", "sxi");
		addMimeType("application/vnd.sun.xml.impress.template", "sti");
		addMimeType("application/vnd.sun.xml.math", "sxm");
		addMimeType("application/vnd.sun.xml.writer", "sxw");
		addMimeType("application/vnd.sun.xml.writer.global", "sxg");
		addMimeType("application/vnd.sun.xml.writer.template", "stw");
		addMimeType("application/vnd.visio", "vsd");
		addMimeType("application/x-abiword", "abw");
		addMimeType("application/x-apple-diskimage", "dmg");
		addMimeType("application/x-bcpio", "bcpio");
		addMimeType("application/x-bittorrent", "torrent");
		addMimeType("application/x-cdf", "cdf");
		addMimeType("application/x-cdlink", "vcd");
		addMimeType("application/x-chess-pgn", "pgn");
		addMimeType("application/x-cpio", "cpio");
		addMimeType("application/x-debian-package", "deb");
		addMimeType("application/x-debian-package", "udeb");
		addMimeType("application/x-director", "dcr");
		addMimeType("application/x-director", "dir");
		addMimeType("application/x-director", "dxr");
		addMimeType("application/x-dms", "dms");
		addMimeType("application/x-doom", "wad");
		addMimeType("application/x-dvi", "dvi");
		addMimeType("application/x-flac", "flac");
		addMimeType("application/x-font", "pfa");
		addMimeType("application/x-font", "pfb");
		addMimeType("application/x-font", "gsf");
		addMimeType("application/x-font", "pcf");
		addMimeType("application/x-font", "pcf.Z");
		addMimeType("application/x-freemind", "mm");
		addMimeType("application/x-futuresplash", "spl");
		addMimeType("application/x-gnumeric", "gnumeric");
		addMimeType("application/x-go-sgf", "sgf");
		addMimeType("application/x-graphing-calculator", "gcf");
		addMimeType("application/x-gtar", "tgz");
		addMimeType("application/x-gtar", "gtar");
		addMimeType("application/x-gtar", "taz");
		addMimeType("application/x-hdf", "hdf");
		addMimeType("application/x-ica", "ica");
		addMimeType("application/x-internet-signup", "ins");
		addMimeType("application/x-internet-signup", "isp");
		addMimeType("application/x-iphone", "iii");
		addMimeType("application/x-iso9660-image", "iso");
		addMimeType("application/x-jmol", "jmz");
		addMimeType("application/x-kchart", "chrt");
		addMimeType("application/x-killustrator", "kil");
		addMimeType("application/x-koan", "skp");
		addMimeType("application/x-koan", "skd");
		addMimeType("application/x-koan", "skt");
		addMimeType("application/x-koan", "skm");
		addMimeType("application/x-kpresenter", "kpr");
		addMimeType("application/x-kpresenter", "kpt");
		addMimeType("application/x-kspread", "ksp");
		addMimeType("application/x-kword", "kwd");
		addMimeType("application/x-kword", "kwt");
		addMimeType("application/x-latex", "latex");
		addMimeType("application/x-lha", "lha");
		addMimeType("application/x-lzh", "lzh");
		addMimeType("application/x-lzx", "lzx");
		addMimeType("application/x-maker", "frm");
		addMimeType("application/x-maker", "maker");
		addMimeType("application/x-maker", "frame");
		addMimeType("application/x-maker", "fb");
		addMimeType("application/x-maker", "book");
		addMimeType("application/x-maker", "fbdoc");
		addMimeType("application/x-mif", "mif");
		addMimeType("application/x-ms-wmd", "wmd");
		addMimeType("application/x-ms-wmz", "wmz");
		addMimeType("application/x-msi", "msi");
		addMimeType("application/x-ns-proxy-autoconfig", "pac");
		addMimeType("application/x-nwc", "nwc");
		addMimeType("application/x-object", "o");
		addMimeType("application/x-oz-application", "oza");
		addMimeType("application/x-pkcs12", "p12");
		addMimeType("application/x-pkcs12", "pfx");
		addMimeType("application/x-pkcs7-certreqresp", "p7r");
		addMimeType("application/x-pkcs7-crl", "crl");
		addMimeType("application/x-quicktimeplayer", "qtl");
		addMimeType("application/x-shar", "shar");
		addMimeType("application/x-shockwave-flash", "swf");
		addMimeType("application/x-stuffit", "sit");
		addMimeType("application/x-sv4cpio", "sv4cpio");
		addMimeType("application/x-sv4crc", "sv4crc");
		addMimeType("application/x-tar", "tar");
		addMimeType("application/x-texinfo", "texinfo");
		addMimeType("application/x-texinfo", "texi");
		addMimeType("application/x-troff", "t");
		addMimeType("application/x-troff", "roff");
		addMimeType("application/x-troff-man", "man");
		addMimeType("application/x-ustar", "ustar");
		addMimeType("application/x-wais-source", "src");
		addMimeType("application/x-wingz", "wz");
		addMimeType("application/x-webarchive", "webarchive");
		addMimeType("application/x-webarchive-xml", "webarchivexml");
		addMimeType("application/x-x509-ca-cert", "crt");
		addMimeType("application/x-x509-user-cert", "crt");
		addMimeType("application/x-xcf", "xcf");
		addMimeType("application/x-xfig", "fig");
		addMimeType("application/xhtml+xml", "xhtml");
		addMimeType("audio/3gpp", "3gpp");
		addMimeType("audio/amr", "amr");
		addMimeType("audio/basic", "snd");
		addMimeType("audio/midi", "mid");
		addMimeType("audio/midi", "midi");
		addMimeType("audio/midi", "kar");
		addMimeType("audio/midi", "xmf");
		addMimeType("audio/mobile-xmf", "mxmf");

		// add ".mp3" first so it will be the default for guessExtensionFromMimeType
		addMimeType("audio/mpeg", "mp3");
		addMimeType("audio/mpeg", "mpga");
		addMimeType("audio/mpeg", "mpega");
		addMimeType("audio/mpeg", "mp2");
		addMimeType("audio/mpeg", "m4a");
		addMimeType("audio/mpegurl", "m3u");
		addMimeType("audio/prs.sid", "sid");
		addMimeType("audio/x-aiff", "aif");
		addMimeType("audio/x-aiff", "aiff");
		addMimeType("audio/x-aiff", "aifc");
		addMimeType("audio/x-gsm", "gsm");
		addMimeType("audio/x-mpegurl", "m3u");
		addMimeType("audio/x-ms-wma", "wma");
		addMimeType("audio/x-ms-wax", "wax");
		addMimeType("audio/x-pn-realaudio", "ra");
		addMimeType("audio/x-pn-realaudio", "rm");
		addMimeType("audio/x-pn-realaudio", "ram");
		addMimeType("audio/x-realaudio", "ra");
		addMimeType("audio/x-scpls", "pls");
		addMimeType("audio/x-sd2", "sd2");
		addMimeType("audio/x-wav", "wav");
		addMimeType("image/bmp", "bmp");
		addMimeType("image/gif", "gif");
		addMimeType("image/ico", "cur");
		addMimeType("image/ico", "ico");
		addMimeType("image/ief", "ief");
		addMimeType("image/jpeg", "jpeg");
		addMimeType("image/jpeg", "jpg");
		addMimeType("image/jpeg", "jpe");
		addMimeType("image/pcx", "pcx");
		addMimeType("image/png", "png");
		addMimeType("image/svg+xml", "svg");
		addMimeType("image/svg+xml", "svgz");
		addMimeType("image/tiff", "tiff");
		addMimeType("image/tiff", "tif");
		addMimeType("image/vnd.djvu", "djvu");
		addMimeType("image/vnd.djvu", "djv");
		addMimeType("image/vnd.wap.wbmp", "wbmp");
		addMimeType("image/x-cmu-raster", "ras");
		addMimeType("image/x-coreldraw", "cdr");
		addMimeType("image/x-coreldrawpattern", "pat");
		addMimeType("image/x-coreldrawtemplate", "cdt");
		addMimeType("image/x-corelphotopaint", "cpt");
		addMimeType("image/x-icon", "ico");
		addMimeType("image/x-jg", "art");
		addMimeType("image/x-jng", "jng");
		addMimeType("image/x-ms-bmp", "bmp");
		addMimeType("image/x-photoshop", "psd");
		addMimeType("image/x-portable-anymap", "pnm");
		addMimeType("image/x-portable-bitmap", "pbm");
		addMimeType("image/x-portable-graymap", "pgm");
		addMimeType("image/x-portable-pixmap", "ppm");
		addMimeType("image/x-rgb", "rgb");
		addMimeType("image/x-xbitmap", "xbm");
		addMimeType("image/x-xpixmap", "xpm");
		addMimeType("image/x-xwindowdump", "xwd");
		addMimeType("model/iges", "igs");
		addMimeType("model/iges", "iges");
		addMimeType("model/mesh", "msh");
		addMimeType("model/mesh", "mesh");
		addMimeType("model/mesh", "silo");
		addMimeType("text/calendar", "ics");
		addMimeType("text/calendar", "icz");
		addMimeType("text/comma-separated-values", "csv");
		addMimeType("text/css", "css");
		addMimeType("text/html", "htm");
		addMimeType("text/html", "html");
		addMimeType("text/h323", "323");
		addMimeType("text/iuls", "uls");
		addMimeType("text/mathml", "mml");

		// add ".txt" first so it will be the default for guessExtensionFromMimeType
		addMimeType("text/plain", "txt");
		addMimeType("text/plain", "asc");
		addMimeType("text/plain", "text");
		addMimeType("text/plain", "diff");
		addMimeType("text/plain", "po");
		addMimeType("text/richtext", "rtx");
		addMimeType("text/rtf", "rtf");
		addMimeType("text/texmacs", "ts");
		addMimeType("text/text", "phps");
		addMimeType("text/tab-separated-values", "tsv");
		addMimeType("text/xml", "xml");
		addMimeType("text/x-bibtex", "bib");
		addMimeType("text/x-boo", "boo");
		addMimeType("text/x-c++hdr", "hpp");
		addMimeType("text/x-c++hdr", "h++");
		addMimeType("text/x-c++hdr", "hxx");
		addMimeType("text/x-c++hdr", "hh");
		addMimeType("text/x-c++src", "cpp");
		addMimeType("text/x-c++src", "c++");
		addMimeType("text/x-c++src", "cc");
		addMimeType("text/x-c++src", "cxx");
		addMimeType("text/x-chdr", "h");
		addMimeType("text/x-component", "htc");
		addMimeType("text/x-csh", "csh");
		addMimeType("text/x-csrc", "c");
		addMimeType("text/x-dsrc", "d");
		addMimeType("text/x-haskell", "hs");
		addMimeType("text/x-java", "java");
		addMimeType("text/x-literate-haskell", "lhs");
		addMimeType("text/x-moc", "moc");
		addMimeType("text/x-pascal", "p");
		addMimeType("text/x-pascal", "pas");
		addMimeType("text/x-pcs-gcd", "gcd");
		addMimeType("text/x-setext", "etx");
		addMimeType("text/x-tcl", "tcl");
		addMimeType("text/x-tex", "tex");
		addMimeType("text/x-tex", "ltx");
		addMimeType("text/x-tex", "sty");
		addMimeType("text/x-tex", "cls");
		addMimeType("text/x-vcalendar", "vcs");
		addMimeType("text/x-vcard", "vcf");
		addMimeType("video/3gpp", "3gpp");
		addMimeType("video/3gpp", "3gp");
		addMimeType("video/3gpp", "3g2");
		addMimeType("video/dl", "dl");
		addMimeType("video/dv", "dif");
		addMimeType("video/dv", "dv");
		addMimeType("video/fli", "fli");
		addMimeType("video/m4v", "m4v");
		addMimeType("video/mpeg", "mpeg");
		addMimeType("video/mpeg", "mpg");
		addMimeType("video/mpeg", "mpe");
		addMimeType("video/mp4", "mp4");
		addMimeType("video/mpeg", "VOB");
		addMimeType("video/quicktime", "qt");
		addMimeType("video/quicktime", "mov");
		addMimeType("video/vnd.mpegurl", "mxu");
		addMimeType("video/x-la-asf", "lsf");
		addMimeType("video/x-la-asf", "lsx");
		addMimeType("video/x-mng", "mng");
		addMimeType("video/x-ms-asf", "asf");
		addMimeType("video/x-ms-asf", "asx");
		addMimeType("video/x-ms-wm", "wm");
		addMimeType("video/x-ms-wmv", "wmv");
		addMimeType("video/x-ms-wmx", "wmx");
		addMimeType("video/x-ms-wvx", "wvx");
		addMimeType("video/x-msvideo", "avi");
		addMimeType("video/x-sgi-movie", "movie");
		addMimeType("video/x-webex", "wrf");
		addMimeType("x-conference/x-cooltalk", "ice");
		addMimeType("x-epoc/x-sisx-app", "sisx");
	}

	private ResourceUtils() {
		/* intentionally left blank */
	}
	
	public static void closeQuietly(Closeable closeable) {
		try {
			closeable.close();
		}
		catch (final IOException ioException) {
			/* shh! quietly! */
		}
	}
	
	public static int copyStreams(InputStream inputStream, OutputStream outputStream, boolean flush) throws IOException {
    	return copyStreams(inputStream, outputStream, flush, ResourceUtils.DEFAULT_BUFFER_SIZE);
    }
	
    public static int copyStreams(InputStream inputStream, OutputStream outputStream, boolean flush, int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];

        int n, total = 0;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            total += n;
        }
        
        if (flush) {
        	outputStream.flush();
        }
        
        return total;
    }

	public static String guessMimeTypeFromExtension(String extension) {
		if (extension == null || extension.isEmpty()) {
			return null;
		}

		return EXTENSION_TO_MIME_TYPE_MAP.get(extension);
	}

	public static String guessMimeTypeFromFilename(String filename) {
		int i = filename.lastIndexOf('.');

		if (i == -1) {
			return null;
		}

		return guessMimeTypeFromExtension(filename.substring(i + 1));
	}

	public static String guessExtensionFromMimeType(String mimeType) {
		if (mimeType == null || mimeType.isEmpty()) {
			return null;
		}

		return MIME_TYPE_TO_EXTENSION_MAP.get(mimeType);
	}

	private static void addMimeType(String mimeType, String extension) {
		if (MIME_TYPE_TO_EXTENSION_MAP.containsKey(mimeType) == false) {
			MIME_TYPE_TO_EXTENSION_MAP.put(mimeType, extension);
		}

		EXTENSION_TO_MIME_TYPE_MAP.put(extension, mimeType);
	}
	
}
