package com.ecom.fyp2023.ModelClasses;

import androidx.annotation.NonNull;

import com.ecom.fyp2023.AppManagers.DiffComputation;

/**
 * Class representing one diff operation.
 */
public class Diff {

    //One of: INSERT, DELETE or EQUAL.
    public DiffComputation.Operation operation;

    public String text;

    public Diff(DiffComputation.Operation operation, String text) {
        // Construct a diff with the specified operation and text.
        this.operation = operation;
        this.text = text;
    }

    //Display a human-readable version of this Diff.
    @NonNull
    public String toString() {
        String prettyText = this.text.replace('\n', '\u00b6');
        return "Diff(" + this.operation + ",\"" + prettyText + "\")";
    }

    //Create a numeric hash value for a Diff.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = (operation == null) ? 0 : operation.hashCode();
        result += prime * ((text == null) ? 0 : text.hashCode());
        return result;
    }

    //Is this Diff equivalent to another Diff?
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Diff other = (Diff) obj;
        if (operation != other.operation) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }
}
