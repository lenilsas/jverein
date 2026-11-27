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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.Variable.SollbuchungMap;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Sollbuchung;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class SollbuchungExportCSV implements Exporter
{

  @Override
  public String getName()
  {
    return "Sollbuchungen CSV-Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (!Sollbuchung.class.isAssignableFrom(objectType))
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return SollbuchungExportCSV.this.getName();
      }

      /**
       * @see de.willuhn.jameica.hbci.io.IOFormat#getFileExtensions()
       */
      @Override
      public String[] getFileExtensions()
      {
        return new String[] { "*.csv" };
      }
    };
    return new IOFormat[] { f };
  }

  @Override
  public void doExport(final Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws DocumentException, IOException, ApplicationException
  {
    try
    {
      ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

      List<Sollbuchung> sollbuchungen = Arrays.stream(objects)
          .map(Sollbuchung.class::cast).collect(Collectors.toList());

      Mitglied m = null;
      Sollbuchung sollb = null;
      if (sollbuchungen.size() > 0)
      {
        sollb = sollbuchungen.get(0);
        m = sollb.getMitglied();
      }
      else
      {
        sollb = (Sollbuchung) Einstellungen.getDBService()
            .createObject(Sollbuchung.class, null);
        m = (Mitglied) Einstellungen.getDBService().createObject(Mitglied.class,
            null);
      }

      Map<String, Object> map = new MitgliedMap().getMap(m, null);
      map = new SollbuchungMap().getMap(sollb, map);
      String[] header = createHeader(map);
      Logger.debug("Header");
      for (String s : header)
      {
        Logger.debug(s);
      }
      CellProcessor[] processors = CellProcessors.createCellProcessors(map);

      writer.writeHeader(header);

      for (Sollbuchung sollbuch : sollbuchungen)
      {
        Map<String, Object> mp = new MitgliedMap()
            .getMap(sollbuch.getMitglied(), null);
        map = new SollbuchungMap().getMap(sollbuch, mp);
        writer.write(map, header, processors);
        monitor.log("Export: "
            + Adressaufbereitung.getNameVorname(sollbuch.getMitglied()));
      }
      writer.close();
    }
    catch (Exception e)
    {
      Logger.error("Fehler", e);
    }
  }

  private String[] createHeader(Map<String, Object> map)
  {
    return map.keySet().toArray(new String[0]);
  }

  @Override
  public String getDateiname(Map<Filter, String> filterMap, Object[] objects, ExportBereich bereich)
  {
    return VorlageUtil.getName(VorlageTyp.SOLLBUCHUNGEN_DATEINAME, filterMap)
        + ".csv";
  }

  @Override
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_NONE;
  }
}
