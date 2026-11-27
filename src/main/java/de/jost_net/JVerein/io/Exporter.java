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
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import com.itextpdf.text.DocumentException;

import de.jost_net.JVerein.keys.Filter;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;
import de.willuhn.util.Session;

/**
 * Basis-Interface aller Exporter. Alle Klassen, die dieses Interface
 * implementieren, werden automatisch von Hibiscus erkannt und dem Benutzer als
 * Export-Moeglichkeit angeboten insofern sie einen parameterlosen Konstruktor
 * mit dem Modifier "public" besitzen (Java-Bean-Konvention).
 */
public interface Exporter extends IO
{
  /**
   * Eine Session fuer zusaetzliche Parameter.
   */
  public final static Session SESSION = new Session();

  public enum ExportBereich
  {
    IS_ANLAGENKONTO,
    IS_NICHTMITGLIEDER
  }

  /**
   * Exportiert die genannten Objekte in den angegebenen OutputStream.
   * 
   * @param objects
   *          die zu exportierenden Objekte.
   * @param filterMap
   * @param file
   *          File-Object für die Ausgabe. Der Exporter muss die Datei selbe
   *          erstellen und schliessen!
   * @param monitor
   *          ein Monitor, an den der Exporter Ausgaben ueber seinen
   *          Bearbeitungszustand ausgeben kann.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void doExport(Object[] objects, Map<Filter, String> filterMap,
      File file, ExportLayoutParam params, ProgressMonitor monitor)
      throws RemoteException, ApplicationException, FileNotFoundException,
      DocumentException, IOException;

  /**
   * Dateiname für Report generieren
   * 
   * @param objects
   * @param bereich
   *          TODO
   * 
   * @throws RemoteException
   */
  public String getDateiname(Map<Filter, String> filterMap, Object[] objects,
      ExportBereich bereich) throws RemoteException;

  /**
   * Titel für Report generieren
   * 
   * @param filterMap
   * @param bereich
   * @return Titel
   */
  default String getTitle(Map<Filter, String> filterMap, ExportBereich bereich)
  {
    return null;
  }

  /**
   * Subtitel für Report generieren
   * 
   * @param filterMap
   * @param bereich
   * @return Subtitle
   */
  default String getSubtitle(Map<Filter, String> filterMap,
      ExportBereich bereich)
  {
    return null;
  }

  /**
   * Sagt, ob eine zweite Tabellenhintergrund Farbe unterstützt wird
   * 
   * @return
   */
  default boolean hasColortable2()
  {
    return false;
  }

  /**
   * Übergabe von Ausgabeparameter für die Reportgenerierung
   * 
   * @return
   */
  default Filter[] getAusgabeParameter()
  {
    return null;
  }

  /**
   * Liefert Optionen für den PDF Teil des Export Dialogs
   * 
   * @return Optionen wie in AbstractPartExportDialog definiert
   */
  public int getExportDialogOptions();

  public default boolean isDirectoryExport()
  {
    return false;
  }
}
