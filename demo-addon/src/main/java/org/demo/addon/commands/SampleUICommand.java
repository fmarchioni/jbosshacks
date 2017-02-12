package org.demo.addon.commands;

import org.jboss.forge.addon.ui.command.AbstractUICommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.util.Categories;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.metadata.WithAttributes;

import javax.inject.Inject;

public class SampleUICommand extends AbstractUICommand {

	@Inject @WithAttributes(label = "Name", required = true, description = "What's your name")  
        UIInput<String> input;
	
	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(SampleUICommand.class).name(
				"SampleUICommand");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		builder.add(input);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		return Results
				.success("Welcome "+input.getValue());
	}
}
