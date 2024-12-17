package it.unibz.inf.ontop.model.term.functionsymbol.impl.raster;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.DBTypeFactory;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import org.apache.commons.rdf.api.IRI;

import javax.annotation.Nonnull;

public class RasterTemporalAverageFunctionSymbolImpl extends AbstractRasterFunctionSymbolImpl{

    public RasterTemporalAverageFunctionSymbolImpl(@Nonnull IRI functionIRI, RDFDatatype xsdDatetime, RDFDatatype xsdStringDatatype, RDFDatatype wktLiteralType) {
        super("RAS_TEMPORAL_AVERAGE", functionIRI, ImmutableList.of(xsdDatetime, xsdDatetime, wktLiteralType, xsdStringDatatype),
                xsdStringDatatype);
    }

    @Override
    protected ImmutableTerm computeDBTerm(ImmutableList<ImmutableTerm> subLexicalTerms, ImmutableList<ImmutableTerm> typeTerms, TermFactory termFactory) {
        //TODO
        // return termFactory.getRESPECTIVEDBFUNCTIONSymbols;  --------------------------------[STEP 01b]-----------------------------------
        DBTypeFactory dbTypeFactory = termFactory.getTypeFactory().getDBTypeFactory();

        return termFactory.getRasterTemporalAverage(subLexicalTerms.get(0), subLexicalTerms.get(1), subLexicalTerms.get(2), subLexicalTerms.get(3));

    }

}