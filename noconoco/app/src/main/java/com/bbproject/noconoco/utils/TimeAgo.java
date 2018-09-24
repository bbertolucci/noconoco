package com.bbproject.noconoco.utils;

import android.content.Context;

import com.bbproject.noconoco.R;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class TimeAgo {

    private static final Map<String, Long> times = new LinkedHashMap<>();

    private static void setStatic(Context pContext) {
        times.put(pContext.getString(R.string.year), TimeUnit.DAYS.toMillis(365));
        times.put(pContext.getString(R.string.month), TimeUnit.DAYS.toMillis(30));
        times.put(pContext.getString(R.string.week), TimeUnit.DAYS.toMillis(7));
        times.put(pContext.getString(R.string.day), TimeUnit.DAYS.toMillis(1));
        times.put(pContext.getString(R.string.hour), TimeUnit.HOURS.toMillis(1));
        times.put(pContext.getString(R.string.minute), TimeUnit.MINUTES.toMillis(1));
        times.put(pContext.getString(R.string.second), TimeUnit.SECONDS.toMillis(1));
    }

    private static String toRelative(Context pContext, long duration, int maxLevel) {
        setStatic(pContext);
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append((timeDelta > 1 && !time.getKey().endsWith("s")) ? pContext.getString(R.string.plural) : "")
                        .append(", ");
                duration -= time.getValue() * timeDelta;
                level++;
            }
            if (level == maxLevel) {
                break;
            }
        }
        if ("".equals(res.toString())) {
            return pContext.getString(R.string.default_time);
        } else {
            res.setLength(res.length() - 2);
            return pContext.getString(R.string.prefix) + res.toString() + pContext.getString(R.string.suffix);
        }
    }


    public static String toRelative(Context pContext, Date start, Date end, int level) {
        return toRelative(pContext, end.getTime() - start.getTime(), level);
    }
}