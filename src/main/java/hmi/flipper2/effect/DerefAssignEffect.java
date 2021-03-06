/*******************************************************************************
 * Copyright (C) 2017-2020 Human Media Interaction, University of Twente, the Netherlands
 *
 * This file is part of the Flipper-2.0 Dialogue Control project.
 *
 * Flipper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flipper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flipper.  If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package hmi.flipper2.effect;

import java.util.HashSet;
import java.util.Set;

import hmi.flipper2.Config;
import hmi.flipper2.FlipperException;
import hmi.flipper2.Is;
import hmi.flipper2.dataflow.DataFlow;

public class DerefAssignEffect extends Effect {

	private String var;
	private String expr;
	
	public DerefAssignEffect(String id, String var, String expr) {
		super(id);
		this.var = var;
		this.expr = expr;
		
	}
	
	public Object doIt(Is is) throws FlipperException {
		is.assignDerefJavascript(var, expr);
		return null;
	}
	
	public Set<String> flowIn() {
		return DataFlow.extractRefs(this.expr);
	}
	
	public Set<String> flowOut() {
		HashSet<String> res = new HashSet<String>();
		res.add(var);
		return res;
	}
	
}
