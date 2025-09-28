package org.objectweb.asm.depend;

import com.example.javac.incremental.entity.FqName;

import java.util.HashSet;
import java.util.Set;

public class DependencyAnalysis {

    protected Set<FqName> types;
    protected Set<FqName> superTypes;

    public DependencyAnalysis() {
        types = new HashSet<>();
        superTypes = new HashSet<>();
    }

    public Set<FqName> getTypes() {
        return Set.copyOf(types);
    }

    public Set<FqName> getSuperTypes() {
        return Set.copyOf(superTypes);
    }
}
