/**********************************************************************
 * basiert auf dem KontoAuswahlDialog aus Hibiscus
 * Copyright (c) by Heiner Jostkleigrewe
 * This program is free software: you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,  but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See 
 *  the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, 
 * see <http://www.gnu.org/licenses/>.
 * 
 * heiner@jverein.de
 * www.jverein.de
 **********************************************************************/

package de.jost_net.JVerein.gui.dialogs;

import java.rmi.RemoteException;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.action.StartViewAction;
import de.jost_net.JVerein.gui.dialogs.BuchungenSollbuchungZuordnungDialog.BookingMemberAccountEntry;
import de.jost_net.JVerein.gui.view.BuchungListeView;
import de.jost_net.JVerein.io.SplitbuchungsContainer;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.CurrencyFormatter;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.hbci.gui.formatter.IbanFormatter;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * Ein Dialog, der die automatisch ermittelten Zuordnungen zwischen Buchung und
 * Sollbuchung anzeigt und bei Bestätigung persistiert
 */
public class BuchungenSollbuchungZuordnungVorschauDialog extends AbstractDialog<Object>
{
  private List<BookingMemberAccountEntry> assignedBooking;

  public BuchungenSollbuchungZuordnungVorschauDialog(List<BookingMemberAccountEntry> assignedBooking)
  {
    super(AbstractDialog.POSITION_CENTER);
    super.setSize(1400, 400);
    this.setTitle("Buchungszuordnung bestätigen");
    this.assignedBooking = assignedBooking;
  }

  @Override
  protected void paint(Composite parent) throws Exception
  {
    final TablePart bu = new TablePart(assignedBooking, null);

    if ((Boolean) Einstellungen.getEinstellung(Property.EXTERNEMITGLIEDSNUMMER))
    {
      bu.addColumn("Ext. Mitgliedsnummer", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "mitglied.externemitgliedsnummer");
    }
    else
    {
      bu.addColumn("Mitgliedsnummer", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "mitglied.id");
    }

    bu.addColumn("Anrede", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "mitglied.anrede");
    bu.addColumn("Vorname", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "mitglied.vorname");
    bu.addColumn("Name", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "mitglied.name");
    bu.addColumn("Abrechnungslaufnummer", BookingMemberAccountEntry.PREFIX_MITGLIEDSKONTO + "abrechnungslauf.id");
    bu.addColumn("Buchungsnummer", BookingMemberAccountEntry.PREFIX_BUCHUNG + "id");
    bu.addColumn("IBAN oder Kontonummer", BookingMemberAccountEntry.PREFIX_BUCHUNG + "iban", new IbanFormatter());
    bu.addColumn("Betrag", BookingMemberAccountEntry.PREFIX_BUCHUNG + "betrag", new CurrencyFormatter("", Einstellungen.DECIMALFORMAT));
    bu.addColumn("Verwendungszweck", BookingMemberAccountEntry.PREFIX_BUCHUNG + "zweck", new Formatter()
    {
      @Override
      public String format(Object value)
      {
        if (value == null)
        {
          return null;
        }
        String s = value.toString();
        s = s.replaceAll("\r\n", " ");
        s = s.replaceAll("\r", " ");
        s = s.replaceAll("\n", " ");
        return s;
      }
    });
    bu.addColumn("Buchungsdatum", BookingMemberAccountEntry.PREFIX_BUCHUNG + "datum", new DateFormatter(new JVDateFormatTTMMJJJJ()));
    bu.addColumn("Zuordnungsart", "zuordnungsart");

    bu.setRememberColWidths(true);
    bu.paint(parent);

    ButtonArea b = new ButtonArea();
    b.addButton("Zuordnen", new Action()
    {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        persistAssignment();
      }
    }, null, false, "ok.png");
    
    b.addButton("Abbrechen", new Action()
    {
      @Override
      public void handleAction(Object context)
      {
        close();
      }
    }, null, false, "process-stop.png");
    b.paint(parent);
  }

  protected void persistAssignment() throws ApplicationException
  {
    try
    {
      for(BookingMemberAccountEntry dao : assignedBooking)
      {
        Sollbuchung sollb = dao.getSollbuchung();
        Buchung buchung = dao.getBuchung();
        SplitbuchungsContainer.autoSplit(buchung, sollb, false);
      }

      //Darstellung aktualisieren
      new StartViewAction(BuchungListeView.class).handleAction(this);

      GUI.getStatusBar().setSuccessText("Die Zuordnung wurde erfolgreich durchgeführt");
    }
    catch (RemoteException e) {
      Logger.error("error while assignment", e);
      throw new ApplicationException("Fehler bei der Durchführung der Zuordnung", e);
    }

    close();
  }

  @Override
  protected Object getData() throws Exception
  {
    return null;
  }
}
