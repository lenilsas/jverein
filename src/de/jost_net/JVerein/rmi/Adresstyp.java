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
package de.jost_net.JVerein.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

public interface Adresstyp extends DBObject
{
  public String getBezeichnung() throws RemoteException;

  public void setBezeichnung(String bezeichnung) throws RemoteException;

  public String getBezeichnungPlural() throws RemoteException;

  public void setBezeichnungPlural(String bezeichnungplural)
      throws RemoteException;

  /**
   * JVerein-ID <br>
   * Mit der JVerein-ID werden Mitgliedstypen mit festen Funktionen in JVerein
   * festgelegt. Beispiele: Mitglied, Spender.
   */
  public int getJVereinid() throws RemoteException;

  public void setJVereinid(int jvereinid) throws RemoteException;
}
