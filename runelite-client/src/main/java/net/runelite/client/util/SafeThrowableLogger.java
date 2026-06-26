/*
 * Copyright (c) 2026, CupidBot
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.util;

import java.io.PrintStream;
import org.slf4j.Logger;

public final class SafeThrowableLogger
{
	private SafeThrowableLogger()
	{
	}

	public static void logUncaught(Logger logger, String message, Throwable throwable)
	{
		logUncaught(logger, message, throwable, System.err);
	}

	static void logUncaught(Logger logger, String message, Throwable throwable, PrintStream err)
	{
		String summary = formatMessage(message, throwable);
		boolean logged = false;

		if (logger != null)
		{
			try
			{
				logger.error(summary);
				logged = true;
			}
			catch (Throwable logFailure)
			{
				printLine(err, summary);
				printLine(err, "Failed to log throwable safely: " + describe(logFailure));
				printStackTrace(err, logFailure);
			}
		}

		if (!logged)
		{
			printLine(err, summary);
		}

		printStackTrace(err, throwable);
	}

	public static String describe(Throwable throwable)
	{
		if (throwable == null)
		{
			return "<null>";
		}

		String className = throwable.getClass().getName();
		String throwableMessage = throwable.getMessage();
		if (throwableMessage == null || throwableMessage.isEmpty())
		{
			return className;
		}

		return className + ": " + throwableMessage;
	}

	private static String formatMessage(String message, Throwable throwable)
	{
		String prefix = message == null ? "" : message.trim();
		String description = describe(throwable);
		if (prefix.isEmpty())
		{
			return description;
		}

		return prefix + " " + description;
	}

	private static void printLine(PrintStream err, String line)
	{
		try
		{
			err.println(line);
		}
		catch (Throwable ignored)
		{
			// Last-chance crash logging must never create another uncaught exception.
		}
	}

	private static void printStackTrace(PrintStream err, Throwable throwable)
	{
		if (throwable == null)
		{
			return;
		}

		try
		{
			throwable.printStackTrace(err);
		}
		catch (Throwable ignored)
		{
			// Last-chance crash logging must never create another uncaught exception.
		}
	}
}
