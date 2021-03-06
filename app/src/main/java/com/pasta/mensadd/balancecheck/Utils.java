/*
 * Utils.java
 *
 * Copyright (C) 2011 Eric Butler
 *
 * Authors:
 * Eric Butler <eric@codebutler.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pasta.mensadd.balancecheck;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.WindowManager;


import com.pasta.mensadd.balancecheck.card.desfire.DesfireException;
import com.pasta.mensadd.balancecheck.card.desfire.DesfireFileSettings;
import com.pasta.mensadd.balancecheck.card.desfire.DesfireProtocol;

import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class Utils {

	private static final String TAG = Utils.class.getName();

    public static void showError (final Activity activity, Exception ex) {
        Log.e(activity.getClass().getName(), ex.getMessage(), ex);
        new AlertDialog.Builder(activity)
            .setMessage(Utils.getErrorMessage(ex))
            .show();
    }

    public static void showErrorAndFinish (final Activity activity, Exception ex) {
        try {
            Log.e(activity.getClass().getName(), Utils.getErrorMessage(ex));
            ex.printStackTrace();

            new AlertDialog.Builder(activity)
                .setMessage(Utils.getErrorMessage(ex))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        activity.finish();
                    }
                })
                .show();
        } catch (WindowManager.BadTokenException unused) {
            /* Ignore... happens if the activity was destroyed */
        }
    }

    public static String getHexString (byte[] b) throws Exception {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static String getHexString (byte[] b, String defaultResult) {
        try {
            return getHexString(b);
        } catch (Exception ex) {
            return defaultResult;
        }
    }

    public static byte[] hexStringToByteArray (String s) {
        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("Bad input string: " + s);
        }
        
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /*
    public static byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    */
    
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }
    
    public static int byteArrayToInt(byte[] b, int offset) {
        return byteArrayToInt(b, offset, b.length);
    }
    
    public static int byteArrayToInt(byte[] b, int offset, int length) {
        return (int) byteArrayToLong(b, offset, length);
    }

    public static long byteArrayToLong(byte[] b, int offset, int length) {
        if (b.length < length)
            throw new IllegalArgumentException("length must be less than or equal to b.length");

        long value = 0;
        for (int i = 0; i < length; i++) {
            int shift = (length - 1 - i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    public static byte[] byteArraySlice(byte[] b, int offset, int length) {
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++)
            ret[i] = b[offset+i];
        return ret;
    }

    public static String xmlNodeToString (Node node) throws Exception {
        // The amount of code required to do simple things in Java is incredible.
        Source source = new DOMSource(node);
        StringWriter stringWriter = new StringWriter();
        Result result = new StreamResult(stringWriter);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setURIResolver(null);
        transformer.transform(source, result);
        return stringWriter.getBuffer().toString();
    }

    public static String getErrorMessage (Throwable ex) {
        String errorMessage = ex.getLocalizedMessage();
        if (errorMessage == null)
            errorMessage = ex.getMessage();
        if (errorMessage == null)
            errorMessage = ex.toString();

        if (ex.getCause() != null) {
            String causeMessage = ex.getCause().getLocalizedMessage();
            if (causeMessage == null)
                causeMessage = ex.getCause().getMessage();
            if (causeMessage == null)
                causeMessage = ex.getCause().toString();

            if (causeMessage != null)
                errorMessage += ": " + causeMessage;
        }

        return errorMessage;
    }


    public static <T> T findInList(List<T> list, Matcher<T> matcher) {
        for (T item : list) {
            if (matcher.matches(item)) {
                return item;
            }
        }
        return null;
    }

    public static interface Matcher<T> {
        public boolean matches(T t);
    }

    public static int convertBCDtoInteger(byte data) {
        return (((data & (char)0xF0) >> 4) * 10) + ((data & (char)0x0F));
    }

    public static int getBitsFromInteger(int buffer, int iStartBit, int iLength) {
        return (buffer >> (iStartBit)) & ((char)0xFF >> (8 - iLength));
    }

    /* Based on function from mfocGUI by 'Huuf' (http://www.huuf.info/OV/) */
    public static int getBitsFromBuffer(byte[] buffer, int iStartBit, int iLength) {
        int iEndBit = iStartBit + iLength - 1;
        int iSByte = iStartBit / 8;
        int iSBit = iStartBit % 8;
        int iEByte = iEndBit / 8;
        int iEBit = iEndBit % 8;

        if (iSByte == iEByte) {
            return (int)(((char)buffer[iEByte] >> (7 - iEBit)) & ((char)0xFF >> (8 - iLength)));
        } else {
            int uRet = (((char)buffer[iSByte] & (char)((char)0xFF >> iSBit)) << (((iEByte - iSByte - 1) * 8) + (iEBit + 1)));

            for (int i = iSByte + 1; i < iEByte; i++) {
                uRet |= (((char)buffer[i] & (char)0xFF) << (((iEByte - i - 1) * 8) + (iEBit + 1)));
            }

            uRet |= (((char)buffer[iEByte] & (char)0xFF)) >> (7 - iEBit);

            return uRet;
        }
    }


	public static DesfireFileSettings selectAppFile(DesfireProtocol tag, int appID, int fileID) {
		try {
			tag.selectApp(appID);
		} catch (DesfireException e) {
			Log.w(TAG,"App not found");
			return null;
		}
		try {
			return tag.getFileSettings(fileID);
		} catch (DesfireException e) {
			Log.w(TAG,"File not found");
			return null;
		}
	}

	public static boolean arrayContains(int[] arr, int item) {
		for (int i: arr)
			if (i==item)
				return true;
		return false;
	}

	public static boolean containsAppFile(DesfireProtocol tag, int appID, int fileID) {
		try {
			tag.selectApp(appID);
		} catch (DesfireException e) {
			Log.w(TAG,"App not found");
			Log.w(TAG, e);
			return false;
		}
		try {
			return arrayContains(tag.getFileList(),fileID);
		} catch (DesfireException e) {
			Log.w(TAG,"File not found");
			Log.w(TAG, e);
			return false;
		}
	}
}
