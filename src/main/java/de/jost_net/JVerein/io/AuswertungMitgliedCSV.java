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
import java.util.ArrayList;
import java.util.Map;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Variable.MitgliedMap;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Mitglied;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedCSV extends AuswertungMitgliedAbstractCSV
{

  @Override
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    ArrayList<Mitglied> mitglieder = new ArrayList<>();
    for (Object o : objects)
    {
      if (o instanceof Mitglied m)
      {
        mitglieder.add(m);
      }
    }
    go(mitglieder, file);
  }

  public void go(ArrayList<Mitglied> mitglieder, File file)
      throws ApplicationException
  {
    try
    {
      ICsvMapWriter writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);

      Mitglied m = null;
      if (mitglieder.size() > 0)
      {
        m = mitglieder.get(0);
      }
      else
      {
        m = (Mitglied) Einstellungen.getDBService().createObject(Mitglied.class,
            null);
      }

      String[] header = createHeader(m);

      Map<String, Object> map = new MitgliedMap().getMap(m, null);

      CellProcessor[] processors = CellProcessors.createCellProcessors(map,
          header);

      writer.writeHeader(header);

      for (Mitglied mit : mitglieder)
      {
        writer.write(new MitgliedMap().getMap(mit, null), header, processors);
      }
      writer.close();
    }
    catch (Exception e)
    {
      Logger.error("Error while creating report", e);
      throw new ApplicationException(
          "Fehler beim Erzeugen des Reports (" + e.getMessage() + ")", e);
    }
  }

  private String[] createHeader(Mitglied m)
  {
    try
    {
      return new MitgliedMap().getMap(m, null).keySet().toArray(new String[0]);
    }
    catch (RemoteException e)
    {
      Logger.error("Fehler", e);
    }
    return null;
  }

  @Override
  public String getName()
  {
    return "Mitgliederliste CSV";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (!Mitglied.class.isAssignableFrom(objectType))
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return AuswertungMitgliedCSV.this.getName();
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
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_NONE;
  }
}
