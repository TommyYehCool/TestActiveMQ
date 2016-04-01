package com.exfantasy.test;

import java.lang.management.ManagementFactory;

public class CommonUtil {

	public static int getProcessId() {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String[] split = name.split("@");
		if (split.length == 2)
			name = split[0];
		return Integer.valueOf(name);
	}
}
