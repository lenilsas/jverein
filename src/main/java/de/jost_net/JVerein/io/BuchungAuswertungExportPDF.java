/**********************************************************************
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
 **********************************************************************/

package de.jost_net.JVerein.io;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.jost_net.JVerein.Einstellungen;
import de.jost_net.JVerein.Einstellungen.Property;
import de.jost_net.JVerein.rmi.Buchung;
import de.jost_net.JVerein.rmi.Buchungsart;
import de.jost_net.JVerein.rmi.Buchungsklasse;

public abstract class BuchungAuswertungExportPDF implements Exporter
{
  protected BigDecimal summe = BigDecimal.ZERO;

  protected BigDecimal summeeinnahmen = BigDecimal.ZERO;

  protected BigDecimal summeausgaben = BigDecimal.ZERO;

  protected BigDecimal summeumbuchungen = BigDecimal.ZERO;

  protected boolean kontonummer_in_buchungsliste = false;

  protected List<Buchungsart> getBuchungsarten(Object[] objects)
      throws RemoteException
  {
    List<Buchungsart> buchungsarten = new ArrayList<>();

    for (Object obj : objects)
    {
      Buchungsart art = ((Buchung) obj).getBuchungsart();

      if (art == null)
      {
        continue;
      }

      boolean vorhanden = false;
      for (Buchungsart x : buchungsarten)
      {
        if (Objects.equals(x.getID(), art.getID()))
        {
          vorhanden = true;
          break;
        }
      }

      if (!vorhanden)
      {
        buchungsarten.add(art);
      }
    }
    return buchungsarten;
  }

  protected List<Buchung> getBuchungenEinerBuchungsart(Object[] buchungen,
      Buchungsart bua, Buchungsklasse bukla) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Object o : buchungen)
    {
      if (o instanceof Buchung b)
      {
        if (b.getBuchungsart() == null
            || !b.getBuchungsart().getNummer().equals(bua.getNummer()))
        {
          continue;
        }

        if ((Boolean) Einstellungen
            .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
        {
          if (b.getBuchungsklasseId() == null
              || !b.getBuchungsklasse().getNummer().equals(bukla.getNummer()))
          {
            continue;
          }
        }
        liste.add(b);
      }
    }
    return liste;
  }

  // Buchungen einer Buchungsart ohne Buchungsklasse
  protected List<Buchung> getBuchungenEinerBuchungsart(Object[] buchungen,
      Buchungsart bua) throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Object o : buchungen)
    {
      if (o instanceof Buchung b)
      {
        if (b.getBuchungsart() == null
            || !b.getBuchungsart().getNummer().equals(bua.getNummer()))
        {
          continue;
        }

        if ((Boolean) Einstellungen
            .getEinstellung(Property.BUCHUNGSKLASSEINBUCHUNG))
        {
          if (b.getBuchungsklasseId() != null)
          {
            continue;
          }
        }
        liste.add(b);
      }
    }
    return liste;
  }

  // Buchungen ohne Buchungsart
  protected List<Buchung> getBuchungenOhneBuchungsart(List<Buchung> buchungen)
      throws RemoteException
  {
    List<Buchung> liste = new ArrayList<>();
    for (Buchung b : buchungen)
    {
      if (b.getBuchungsart() != null)
      {
        continue;
      }
      liste.add(b);
    }
    return liste;
  }
}
