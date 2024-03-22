package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class ClipRasterFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl {
    //    protected final FunctionSymbolFactory functionSymbolFactory;

    public ClipRasterFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdIntegerDatatype, RDFDatatype wktLiteralType, RDFDatatype xsdStringDatatype) {
        super("RAS_CLIP_RASTER_SPATIAL", functionIRI, ImmutableList.of(xsdIntegerDatatype, wktLiteralType, xsdStringDatatype), xsdStringDatatype);
        //this.functionSymbolFactory = functionSymbolFactory;
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 04b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getClipRaster(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2));
//        return null;

    }
}
