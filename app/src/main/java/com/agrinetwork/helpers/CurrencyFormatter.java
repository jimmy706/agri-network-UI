package com.agrinetwork.helpers;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    private static final NumberFormat nf = NumberFormat.getInstance(new Locale("VI","vi"));
    public static final String CURRENCY_SUFFIX = "đ";

    private CurrencyFormatter() {}

    public static String format(BigDecimal number) {
        return nf.format(number);
    }

    public static String format(double number) {
        return nf.format(number);
    }

}
