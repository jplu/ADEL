package fr.eurecom.adel.datatypes;

/**
 * @author Julien Plu
 */
public class ConllEntry {
    private String token;
    private String category;
    private Integer begin;
    private Integer end;

    public ConllEntry() {
        this.category = "O";
    }
    
    public ConllEntry(final String newToken, final Integer newBegin, final Integer newEnd) {
        this.token = newToken;
        this.begin = newBegin;
        this.end = newEnd;
        this.category = "O";
    }
    
    public final Integer getBegin() {
        return this.begin;
    }
    
    public final Integer getEnd() {
        return this.end;
    }
    
    public final String getToken() {
        return this.token;
    }

    public final String getCategory() {
        return this.category;
    }

    public final void setCategory(final String newCategory) {
        this.category = newCategory;
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if ((obj == null) || (this.getClass() != obj.getClass())) {
            return false;
        }
        
        final ConllEntry that = (ConllEntry) obj;
        
        if (!this.token.equals(that.token)) {
            return false;
        }
        
        if (!this.category.equals(that.category)) {
            return false;
        }
        
        if (!this.begin.equals(that.begin)) {
            return false;
        }
    
        return this.end.equals(that.end);
    }
    
    @Override
    public final int hashCode() {
        int result = this.token.hashCode();
    
        result = 31 * (result + this.category.hashCode());
        result = 31 * (result + this.begin.hashCode());
        result = 31 * (result + this.end.hashCode());
        
        return result;
    }
    
    @Override
    public final String toString() {
        return "ConllEntry{"
            + "token='" + this.token + '\''
            + ", category='" + this.category + '\''
            + ", begin=" + this.begin
            + ", end=" + this.end
            + '}';
    }
}
