package fr.eurecom.adel.shell.setting;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class SimplePromptProvider implements PromptProvider {
  @Override
  public AttributedString getPrompt() {
    return new AttributedString("adel-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
  }
}
