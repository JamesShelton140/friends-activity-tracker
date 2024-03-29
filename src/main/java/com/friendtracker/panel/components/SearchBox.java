/*
 * Copyright (c) 2021, Tyler Hardy
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
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.friendtracker.panel.components;

import java.awt.Dimension;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;

public class SearchBox extends IconTextField
{
	private SearchBoxCallback fn;

	public SearchBox()
	{
		this.setIcon(Icon.SEARCH);
		this.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH-20, 30));
		this.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		this.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
		this.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent documentEvent)
			{
				fn.call();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent)
			{
				fn.call();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent)
			{
			}
		});

		this.addActionListener(e -> fn.call()
		);
	}

	public void addTextChangedListener(SearchBoxCallback fn)
	{
		this.fn = fn;
	}

	public interface SearchBoxCallback
	{
		void call();
	}
}
