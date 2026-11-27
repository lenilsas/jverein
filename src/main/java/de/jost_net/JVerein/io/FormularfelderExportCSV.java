/**********************************************************************
 * JVerein - Mitgliederverwaltung und einfache Buchhaltung für Vereine
 * Copyright (c) by Heiner Jostkleigrewe
 * Copyright (c) 2014 by Thomas Hooge
 * Main Project: heiner@jverein.dem  http://www.jverein.de/
 * Module Author: thomas@hoogi.de, http://www.hoogi.de/
 *
 * This file is part of JVerein.
 *
 * JVerein is free software: you can redistribute it and/or modify 
 * it under the terms of the  GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JVerein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 **********************************************************************/
package de.jost_net.JVerein.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.keys.VorlageTyp;
import de.jost_net.JVerein.rmi.Formular;
import de.jost_net.JVerein.rmi.Formularfeld;
import de.jost_net.JVerein.util.VorlageUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class FormularfelderExportCSV implements Exporter
{

  private ICsvMapWriter writer;

  private String[] header;

  @Override
  public String getName()
  {
    return "Formularfelder CSV-Export";
  }

  @Override
  public IOFormat[] getIOFormats(Class<?> objectType)
  {
    if (!Formular.class.isAssignableFrom(objectType))
    {
      return null;
    }
    IOFormat f = new IOFormat()
    {

      @Override
      public String getName()
      {
        return FormularfelderExportCSV.this.getName();
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
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws ApplicationException, DocumentException, IOException
  {
    Logger.debug(String.format("Formularfelder Export"));

    try
    {
      writer = new CsvMapWriter(new FileWriter(file),
          CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
      header = new String[] { "Name", "Seite", "X", "Y", "Font", "Fontsize",
          "Fontstyle", "Ausrichtung" };
      writer.writeHeader(header);
    }
    catch (IOException e)
    {
      throw new DocumentException(e);
    }

    if (objects.length > 1)
    {
      throw new ApplicationException(
          "Nur Export der Formularfelder von einem Formular möglich");
    }
    GenericIterator<Formularfeld> it = ((Formular) objects[0])
        .getFormularfelder(0);
    while (it.hasNext())
    {
      Formularfeld ff = it.next();
      Map<String, Object> ffmap = new HashMap<>();
      ffmap.put("Name", ff.getName());
      ffmap.put("Seite", ff.getSeite());
      ffmap.put("X", ff.getX());
      ffmap.put("Y", ff.getY());
      ffmap.put("Font", ff.getFont());
      ffmap.put("Fontsize", ff.getFontsize());
      ffmap.put("Fontstyle", ff.getFontstyle());
      ffmap.put("Ausrichtung", ff.getAusrichtung().getKey());
      writer.write(ffmap, header);

    }
    writer.close();
  }

  @Override
  public String getDateiname(Map<Filter, String> filterMap, Object[] objects)
  {
    try
    {
      return VorlageUtil.getName(VorlageTyp.FORMULARFELDER_DATEINAME,
          ((Formular) objects[0]).getBezeichnung()) + ".csv";
    }
    catch (RemoteException e)
    {
      return "Formularfelder.csv";
    }
  }

  @Override
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_NONE;
  }

}
