/* Copyright 2011 - iSencia Belgium NV

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.isencia.passerelle.core;

import java.io.Serializable;

import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;

public class PasserelleType implements Type, Serializable {

    /** 
	 * The Passerelle Msg type: the least upper bound of all Passerelle Msg types.
	 */
	public static final Type PASSERELLE_MSG_TYPE = new PasserelleType();

	// The constructor is private to make a type safe enumeration.
    // We could extend BaseType, yet the BaseType(Class, String)
    // Constructor is private.
    private PasserelleType() {
        super();
    }

    /** Return a new type which represents the type that results from
     *  adding a token of this type and a token of the given argument
     *  type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type add(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }


    public Object clone() {
        return this;
    }

    public Token convert(Token token)
            throws IllegalActionException {
        if (token instanceof PasserelleToken) {
            return token;
        } else {
            throw new IllegalActionException("Attempt to convert token "
                    + token +
                    " into a Passerelle token, which is not possible.");
        }
    }

    /** Return a new type which represents the type that results from
     *  dividing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type divide(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return the class for tokens that this basetype represents.
     */
    public Class getTokenClass() {
        return PasserelleToken.class;
    }

    public boolean isCompatible(Type type) {
        return getClass().isInstance(type);
    }

    public boolean isConstant() {
        return true;
    }

    /** 
     * @return this type's node index in the (constant) type lattice.
     */
    public int getTypeHash() {
        return Type.HASH_INVALID;
    }

    /** Return a new type which represents the type that results from
     *  moduloing a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type modulo(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return a new type which represents the type that results from
     *  multiplying a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type multiply(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }


    public boolean isInstantiable() {
        return true;
    }

    public boolean isSubstitutionInstance(Type type) {
        return getClass().isInstance(type);
    }

    /** Return the type of the multiplicative identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type one() {
        return this;
    }

    /** Return a new type which represents the type that results from
     *  subtracting a token of this type and a token of the given
     *  argument type.
     *  @param rightArgumentType The type to add to this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type subtract(Type rightArgumentType) {
        return TypeLattice.leastUpperBound(this, rightArgumentType);
    }

    /** Return the type of the additive identity for elements of
     *  this type.
     *  @return A new type, or BaseType.GENERAL, if the operation does
     *  not make sense for the given types.
     */
    public Type zero() {
        return this;
    }

    /** Return the string representation of this type.
     *  @return A String.
     */
    public String toString() {
        return "Passerelle";
    }

	public boolean isAbstract() {
		return false;
	}
}