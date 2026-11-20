package de.jost_net.JVerein.gui.parts;

import de.jost_net.JVerein.gui.action.ExportAction;

public class ExportButton extends ButtonRtoL
{
  public ExportButton(String doku, Object control)
  {
    super("Export", new ExportAction(doku), control, false, "document-save.png",
        "Ctrl+Shift+E");
  }
}
