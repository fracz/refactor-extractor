package com.nhn.hippo.web.vo.callstacks;

import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;

/**
 * each stack
 *
 * @author netspider
 *
 */
public class Record {
	private final int tab;
	private final boolean method;

	private final String title;
	private final String arguments;
	private final long begin;
	private final long elapsed;
	private final String agent;
	private final String service;
	private final boolean excludeFromTimeline;

	public Record(int tab, boolean method, String title, String arguments, long begin, long elapsed, String agent, String service, ServiceType serviceType) {
		this.tab = tab;
		this.method = method;

		this.title = title;
		this.arguments = arguments;
		this.begin = begin;
		this.elapsed = elapsed;
		this.agent = agent;
		this.service = service;
		this.excludeFromTimeline = serviceType == null || serviceType.isInternalMethod();
	}

	public int getTab() {
		return tab;
	}

	public boolean isMethod() {
		return method;
	}

	public String getTitle() {
		return title;
	}

	public String getArguments() {
		return arguments;
	}

	public long getBegin() {
		return begin;
	}

	public long getElapsed() {
		return elapsed;
	}

	public String getAgent() {
		return agent;
	}

	public String getService() {
		return service;
	}

	public boolean isExcludeFromTimeline() {
		return excludeFromTimeline;
	}

	@Override
	public String toString() {
		return "Record [tab=" + tab + ", method=" + method + ", title=" + title + ", arguments=" + arguments + ", begin=" + begin + ", elapsed=" + elapsed + ", agent=" + agent + ", service=" + service + "]";
	}
}