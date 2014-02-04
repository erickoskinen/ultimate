/*
 * Copyright (C) 2009-2014 University of Freiburg
 *
 * This file is part of the ULTIMATE Automata Library.
 *
 * The ULTIMATE Automata Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ULTIMATE Automata Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Automata Library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Automata Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Automata Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.automata.petrinet.visualization;

import java.util.HashMap;

import de.uni_freiburg.informatik.ultimate.automata.Activator;
import de.uni_freiburg.informatik.ultimate.automata.petrinet.julian.Event;
import de.uni_freiburg.informatik.ultimate.model.annotation.DefaultAnnotations;
import de.uni_freiburg.informatik.ultimate.model.annotation.IAnnotations;

/**
 * Ultimate model of a OcurrenceNet event.
 * @author heizmann@informatik.uni-freiburg.de 
 */

public class EventNode<S,C> extends PetriNetVisualizationNode {

	private static final long serialVersionUID = -2531826841396458461L;
	
	
	public EventNode(Event<S,C> event) {
		super(event.getTransition().getSymbol().toString());
		
		DefaultAnnotations annot = new DefaultAnnotations();
		annot.put("Transition",event.getTransition());
		annot.put("Companion", event.getCompanion());
		annot.put("Ancestors", event.getAncestors());
		annot.put("ByLocalConfigurationRepresentedMarking",event.getMark());
	
		HashMap<String,IAnnotations> annotations =  this.getPayload().getAnnotations();
		annotations.put(Activator.PLUGIN_ID, annot);
		
		S symbol = event.getTransition().getSymbol();
		if (symbol instanceof IAnnotations) {
			annot.put("Symbol", (IAnnotations) symbol);

		}
		
	}


}
