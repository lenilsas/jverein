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
import java.rmi.RemoteException;
import java.util.Map;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.gui.dialogs.AbstractPartExportDialog;
import de.jost_net.JVerein.io.Adressbuch.Adressaufbereitung;
import de.jost_net.JVerein.keys.Filter;
import de.jost_net.JVerein.rmi.Mitglied;
import de.jost_net.JVerein.rmi.Mitgliedstyp;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

public class AuswertungMitgliedAdresslistePDF
    extends AuswertungMitgliedAbstractPDF
{

  @Override
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException
  {
    try
    {
      String mitgliedstyp = "";
      if (objects.length > 0 && objects[0] instanceof Mitglied m)
      {
        // wenn es den Mitgliedstyp-Filter nicht gibt, und in der Liste
        // Nicht-Mitglieder sind, ist es die ungefilterte Nicht-Mitglied Liste.
        if (filterMap.get(Filter.MITGLIEDSTYP) == null
            && !Mitgliedstyp.MITGLIED.equals(m.getMitgliedstyp().getID()))
        {
          mitgliedstyp = "Nicht-Mitglieder";
        }
        else
        {
          mitgliedstyp = m.getMitgliedstyp().getBezeichnungPlural();
        }
      }

      FileOutputStream fos = new FileOutputStream(file);
      Reporter reporter = new Reporter(fos, params);

      reporter.addHeaderColumn("Name", Element.ALIGN_CENTER, 60,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Adresse", Element.ALIGN_CENTER, 100,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Telefon", Element.ALIGN_CENTER, 50,
          params.getColorHeader(), params.getFontHeader());
      reporter.addHeaderColumn("Email", Element.ALIGN_CENTER, 80,
          params.getColorHeader(), params.getFontHeader());
      if ((Boolean) Einstellungen.getEinstellung(Property.GEBURTSDATUMPFLICHT))
        reporter.addHeaderColumn("Geburtsdatum", Element.ALIGN_CENTER, 30,
            params.getColorHeader(), params.getFontHeader());
      reporter.createHeader(100, Element.ALIGN_CENTER);

      for (Object o : objects)
      {
        if (o instanceof Mitglied m)
        {
          reporter.addColumn(Adressaufbereitung.getNameVorname(m),
              Element.ALIGN_LEFT, params.getFontNormal());
          reporter.addColumn(Adressaufbereitung.getAnschrift(m),
              Element.ALIGN_LEFT, params.getFontNormal());
          String telefon = "";
          if (m.getTelefonprivat() != null && m.getTelefonprivat().length() > 0)
          {
            telefon = m.getTelefonprivat();
          }
          if (m.getTelefondienstlich() != null
              && m.getTelefondienstlich().length() > 0)
          {
            telefon += "\n" + "dienstl: " + m.getTelefondienstlich();
          }
          if (m.getHandy() != null && m.getHandy().length() > 0)
          {
            telefon += "\n" + "Handy: " + m.getHandy();
          }
          reporter.addColumn(telefon, Element.ALIGN_LEFT,
              params.getFontNormal());
          // Bei verwendung von mehreren Mailadresse im Fomar
          // NAME:Mail1@xx.de,mail2@xx.de; trennen wir die Mailadressen
          String mail = m.getEmail();
          if (mail.indexOf(":") > 0)
          {
            mail = mail.substring(mail.indexOf(":") + 1).replace(",", "\n")
                .replace(";", "").trim();
          }
          reporter.addColumn(mail, Element.ALIGN_LEFT, params.getFontNormal());
          if ((Boolean) Einstellungen
              .getEinstellung(Property.GEBURTSDATUMPFLICHT))
            reporter.addColumn(m.getGeburtsdatum(), Element.ALIGN_LEFT,
                params.getFontNormal());
        }
      }
      reporter.closeTable();

      reporter.add(new Paragraph(
          String.format("Anzahl %s: %d", mitgliedstyp, objects.length),
          params.getFontNormal()));
      if (params.getFilterParameter())
      {
        reporter.addParams(filterMap, params);
      }
      reporter.close();
      fos.close();
      GUI.getStatusBar().setSuccessText(
          String.format("Auswertung fertig. %d Sätze.", objects.length));
    }
    catch (

    Exception e)
    {
      Logger.error("error while creating report", e);
      throw new ApplicationException("Fehler", e);
    }
  }

  @Override
  public String getName()
  {
    return "Adressliste PDF";
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
        return AuswertungMitgliedAdresslistePDF.this.getName();
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
  public int getExportDialogOptions()
  {
    return AbstractPartExportDialog.OPTION_FILTER_PARAMS;
  }
}
