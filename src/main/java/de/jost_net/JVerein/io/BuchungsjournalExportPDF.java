/**********************************************************************
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

package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.gui.dialogs.BuchungsjournalSortDialog;
import de.jost_net.JVerein.gui.formatter.BuchungsartFormatter;
import de.jost_net.JVerein.keys.ArtBuchungsart;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.JVDateFormatTTMMJJJJ;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungsjournalExportPDF implements Exporter
{
  boolean geldkonto;

  private ExportLayoutParam params;

  @Override
  public String getName()
  {
    return "PDF Buchungsjournal";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (!Buchung.class.isAssignableFrom(objectType))
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return BuchungsjournalExportPDF.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.pdf" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public String getDateiname(Map<Filter, String> filterMap, Object[] objects,
      ExportBereich bereich) throws RemoteException
  {

    if (bereich != ExportBereich.IS_ANLAGENKONTO)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_DATEINAME,
          filterMap) + ".pdf";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGSJOURNAL_DATEINAME,
          filterMap) + ".pdf";
    }
  }

  @Override
  public String getTitle(Map<Filter, String> filterMap, ExportBereich bereich)
  {
    if (bereich != ExportBereich.IS_ANLAGENKONTO)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_TITEL, filterMap);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGSJOURNAL_TITEL,
          filterMap);
    }
  }

  @Override
  public String getSubtitle(Map<Filter, String> filterMap,
      ExportBereich bereich)
  {
    if (bereich != ExportBereich.IS_ANLAGENKONTO)
    {
      return VorlageUtil.getName(VorlageTyp.BUCHUNGSJOURNAL_SUBTITEL,
          filterMap);
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_BUCHUNGSJOURNAL_SUBTITEL,
          filterMap);
    }
  }

  @Override
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    this.params = params;

    BuchungsjournalSortDialog djs = new BuchungsjournalSortDialog(
        BuchungsjournalSortDialog.POSITION_CENTER);
    String sort = null;
    try
    {
      sort = djs.open();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
    if (djs.getClosed())
    {
      throw new OperationCanceledException("Ausgabe abgebrochen");
    }
    if (sort != null)
    {
      sortBuchungen(objects, sort);
    }

    FileOutputStream fos = new FileOutputStream(file);
    Reporter reporter = new Reporter(fos, params);

    BigDecimal einnahmen = BigDecimal.ZERO;
    BigDecimal ausgaben = BigDecimal.ZERO;
    BigDecimal umbuchungen = BigDecimal.ZERO;
    BigDecimal nichtzugeordnet = BigDecimal.ZERO;

    createTableHeader(reporter);

    for (Object o : objects)
    {
      if (o instanceof Buchung b)
      {
        reporter.addColumn(b.getID(), Element.ALIGN_RIGHT,
            params.getFontNormal());
        reporter.addColumn(new JVDateFormatTTMMJJJJ().format(b.getDatum()),
            Element.ALIGN_LEFT, params.getFontNormal());
        reporter.addColumn(b.getKonto().getNummer(), Element.ALIGN_RIGHT,
            params.getFontNormal());
        if (b.getAuszugsnummer() != null)
        {
          reporter.addColumn(
              b.getAuszugsnummer() + "/"
                  + (b.getBlattnummer() != null ? b.getBlattnummer() : "-"),
              Element.ALIGN_LEFT, params.getFontNormal());
        }
        else
        {
          reporter.addColumn("", Element.ALIGN_LEFT);
        }
        reporter.addColumn(b.getName(), Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(b.getZweck(), Element.ALIGN_LEFT,
            params.getFontNormal());
        String buklaString = "-: ";
        String buaString = "--";
        if ((Boolean) Einstellungen
            .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
        {
          if (b.getBuchungsklasseId() != null)
          {
            buklaString = b.getBuchungsklasse().getNummer() + ": ";
          }
        }
        else
        {
          if (b.getBuchungsartId() != null
              && b.getBuchungsart().getBuchungsklasseId() != null)
          {
            buklaString = b.getBuchungsart().getBuchungsklasse().getNummer()
                + ": ";
          }
        }
        if (b.getBuchungsartId() != null)
        {
          buaString = new BuchungsartFormatter().format(b.getBuchungsart());
        }
        reporter.addColumn(buklaString + buaString, Element.ALIGN_LEFT,
            params.getFontNormal());
        reporter.addColumn(b.getBetrag(), params.getFontNormal(),
            params.getNegativRot());
        if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
        {
          reporter.addColumn(
              b.getSteuer() == null ? null : b.getSteuer().getSatz(),
              params.getFontNormal(), params.getNegativRot());
        }
        if (b.getBuchungsart() != null)
        {
          int buchungsartart = b.getBuchungsart().getArt();
          switch (buchungsartart)
          {
            case ArtBuchungsart.EINNAHME:
            {
              einnahmen = einnahmen.add(b.getBetrag());
              break;
            }
            case ArtBuchungsart.AUSGABE:
            {
              ausgaben = ausgaben.add(b.getBetrag());
              break;
            }
            case ArtBuchungsart.UMBUCHUNG:
            {
              umbuchungen = umbuchungen.add(b.getBetrag());
              break;
            }
          }
        }
        else
        {
          nichtzugeordnet = nichtzugeordnet.add(b.getBetrag());
        }
      }
    }

    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, 9);
    }
    else
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, 8);
    }

    reporter.addColumn("Summe Einnahmen", Element.ALIGN_LEFT,
        params.getColorTable(), true, params.getFontFett(), 7);
    reporter.addColumn(einnahmen, params.getColorTable(), params.getFontFett(),
        params.getNegativRot());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, params.getColorTable());
    }

    reporter.addColumn("Summe Ausgaben", Element.ALIGN_LEFT,
        params.getColorTable(), true, params.getFontFett(), 7);
    reporter.addColumn(ausgaben, params.getColorTable(), params.getFontFett(),
        params.getNegativRot());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, params.getColorTable());
    }

    reporter.addColumn("Summe Umbuchungen", Element.ALIGN_LEFT,
        params.getColorTable(), true, params.getFontFett(), 7);
    reporter.addColumn(umbuchungen, params.getColorTable(),
        params.getFontFett(), params.getNegativRot());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, params.getColorTable());
    }

    reporter.addColumn("Summe nicht zugeordnet", Element.ALIGN_LEFT,
        params.getColorTable(), true, params.getFontItalic(), 7);
    reporter.addColumn(nichtzugeordnet, params.getColorTable(),
        params.getFontItalic(), params.getNegativRot());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addColumn(" ", Element.ALIGN_LEFT, params.getColorTable());
    }

    reporter.closeTable();
    if (params.getFilterParameter())
    {
      reporter.addParams(filterMap, params);
    }
    reporter.close();
    fos.close();
  }

  private void createTableHeader(Reporter reporter)
      throws DocumentException, RemoteException
  {
    reporter.addHeaderColumn("Nr", Element.ALIGN_RIGHT, 20,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Datum", Element.ALIGN_CENTER, 45,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Konto", Element.ALIGN_CENTER, 40,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Auszug", Element.ALIGN_CENTER, 20,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 90,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Zahlungsgrund", Element.ALIGN_CENTER, 100,
        params.getColorHeader(), params.getFontHeader());
    reporter.addHeaderColumn("Buchungsklasse Nummer:\nBuchungsart",
        Element.ALIGN_CENTER, 70, params.getColorHeader(),
        params.getFontHeader());
    reporter.addHeaderColumn("Betrag", Element.ALIGN_CENTER, 50,
        params.getColorHeader(), params.getFontHeader());
    if ((Boolean) Einstellungen.getEinstellung(Property.STEUERINBUCHUNG))
    {
      reporter.addHeaderColumn("Steuersatz", Element.ALIGN_CENTER, 30,
          params.getColorHeader(), params.getFontHeader());
    }
    reporter.createHeader();
  }

  private void sortBuchungen(Object[] objects, String sort)
      throws RemoteException
  {
    try
    {
      Arrays.sort(objects, (o1, o2) -> {
        try
        {
          Buchung b1 = (Buchung) o1;
          Buchung b2 = (Buchung) o2;

          int result;

          switch (sort)
          {
            case BuchungsjournalSortDialog.ID:
              return compareString(b1.getID(), b2.getID());

            case BuchungsjournalSortDialog.DATUM:
              return b1.getDatum().compareTo(b2.getDatum());

            case BuchungsjournalSortDialog.DATUM_NAME:
              result = b1.getDatum().compareTo(b2.getDatum());
              return result != 0 ? result
                  : compareString(b1.getName(), b2.getName());

            case BuchungsjournalSortDialog.DATUM_ID:
              result = b1.getDatum().compareTo(b2.getDatum());
              return result != 0 ? result
                  : compareString(b1.getID(), b2.getID());

            case BuchungsjournalSortDialog.DATUM_ID_NAME:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareString(b1.getID(), b2.getID());
              return result != 0 ? result
                  : compareString(b1.getName(), b2.getName());

            case BuchungsjournalSortDialog.DATUM_AUSZUGSNUMMER:
              result = b1.getDatum().compareTo(b2.getDatum());
              return result != 0 ? result
                  : compareInteger(b1.getAuszugsnummer(),
                      b2.getAuszugsnummer());

            case BuchungsjournalSortDialog.DATUM_AUSZUGSNUMMER_NAME:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareInteger(b1.getAuszugsnummer(),
                  b2.getAuszugsnummer());
              return result != 0 ? result
                  : compareString(b1.getName(), b2.getName());

            case BuchungsjournalSortDialog.DATUM_BLATTNUMMER:
              result = b1.getDatum().compareTo(b2.getDatum());
              return result != 0 ? result
                  : compareInteger(b1.getBlattnummer(), b2.getBlattnummer());

            case BuchungsjournalSortDialog.DATUM_BLATTNUMMER_NAME:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareInteger(b1.getBlattnummer(), b2.getBlattnummer());
              return result != 0 ? result
                  : compareString(b1.getName(), b2.getName());

            case BuchungsjournalSortDialog.DATUM_AUSGZUGSNUMMER_ID:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareInteger(b1.getAuszugsnummer(),
                  b2.getAuszugsnummer());
              return result != 0 ? result
                  : compareString(b1.getID(), b2.getID());

            case BuchungsjournalSortDialog.DATUM_BLATTNUMMER_ID:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareInteger(b1.getBlattnummer(), b2.getBlattnummer());
              return result != 0 ? result
                  : compareString(b1.getID(), b2.getID());

            case BuchungsjournalSortDialog.DATUM_AUSGZUGSNUMMER_BLATTNUMMER_ID:
              result = b1.getDatum().compareTo(b2.getDatum());
              if (result != 0)
                return result;
              result = compareInteger(b1.getAuszugsnummer(),
                  b2.getAuszugsnummer());
              if (result != 0)
                return result;
              result = compareInteger(b1.getBlattnummer(), b2.getBlattnummer());
              return result != 0 ? result
                  : compareString(b1.getID(), b2.getID());

            default:
              return b1.getDatum().compareTo(b2.getDatum());
          }
        }
        catch (RemoteException e)
        {
          throw new RuntimeException(e);
        }
      });
    }
    catch (RuntimeException e)
    {
      if (e.getCause() instanceof RemoteException re)
        throw re;
      throw e;
    }
  }

  private int compareString(String s1, String s2)
  {
    if (s1 == null)
      return s2 == null ? 0 : -1;
    if (s2 == null)
      return 1;
    return s1.compareToIgnoreCase(s2);
  }

  private int compareInteger(Integer i1, Integer i2)
  {
    if (i1 == null)
      return i2 == null ? 0 : -1;
    if (i2 == null)
      return 1;
    return i1.compareTo(i2);
  }

  @Override
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_FILTER_PARAMS;
  }
}
