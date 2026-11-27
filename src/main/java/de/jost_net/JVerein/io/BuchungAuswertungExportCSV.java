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
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Variable.BuchungMap;
import de.jost_net.JVerein.gui.control.BuchungsControl;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class BuchungAuswertungExportCSV implements Exporter
{
  @Override
  public String getName()
  {
    return "CSV Buchungen";
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
        return BuchungAuswertungExportCSV.this.getName();
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
  public String getDateiname(Map<Filter, String> filterMap, Object[] objects)
      throws RemoteException
  { // TODO Anlagebuchungen?

    if (filterMap instanceof BuchungsControl bcontrol
        && bcontrol.getGeldkonto())
    {
      return VorlageUtil.getName(VorlageTyp.CSVBUCHUNGEN_DATEINAME, filterMap)
          + ".csv";
    }
    else
    {
      return VorlageUtil.getName(VorlageTyp.ANLAGEN_CSVBUCHUNGEN_DATEINAME,
          filterMap) + ".csv";
    }
  }

  @Override
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
        CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

    String[] header = createHeader();

    Map<String, Object> map = BuchungMap.getDummyMap(new HashMap<>());
    CellProcessor[] processors = CellProcessors.createCellProcessors(map);

    writer.writeHeader(header);

    for (Object o : objects)
    {
      if (o instanceof Buchung b)
      {
        writer.write(new BuchungMap().getMap(b, null), header, processors);
      }
    }
    writer.close();
  }

  private String[] createHeader()
  {
    return BuchungMap.getDummyMap(new HashMap<>()).keySet()
        .toArray(new String[0]);
  }

  @Override
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_NONE;
  }
}
