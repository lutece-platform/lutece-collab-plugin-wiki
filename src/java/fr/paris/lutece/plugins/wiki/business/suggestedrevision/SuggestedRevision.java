/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.wiki.business.suggestedrevision;

import java.io.Serializable;
import java.sql.Timestamp;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/**
 * Business class for a SuggestedRevision: a modification proposed by a user
 * who does not have edit rights on the target wiki item, awaiting approval.
 */
public class SuggestedRevision implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_TYPE = "WIKI_SUGGESTED_REVISION";

    private int _nId;
    private int _nEntityId;
    private int _nParentRevisionId;

    @NotEmpty( message = "#i18n{wiki.validation.revision.Title.notEmpty}" )
    @Size( max = 255, message = "#i18n{wiki.validation.revision.Title.size}" )
    private String _strTitle;

    private String _strDescription;
    private String _strContent;

    @Size( max = 500, message = "#i18n{wiki.validation.revision.Comment.size}" )
    private String _strComment;

    @Size( max = 100, message = "#i18n{wiki.validation.revision.Author.size}" )
    private String _strAuthor;

    private String _strAuthorGuid;
    private SuggestionStatus _status = SuggestionStatus.PENDING;
    private String _strReviewComment;
    private String _strReviewer;
    private Timestamp _dateCreation;
    private Timestamp _dateReview;

    /**
     * Default constructor.
     */
    public SuggestedRevision( )
    {
    }

    /**
     * Returns the suggestion identifier.
     *
     * @return the id
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the suggestion identifier.
     *
     * @param nId
     *            the id
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the wiki item id this suggestion targets.
     *
     * @return the entity id
     */
    public int getEntityId( )
    {
        return _nEntityId;
    }

    /**
     * Sets the wiki item id this suggestion targets.
     *
     * @param nEntityId
     *            the entity id
     */
    public void setEntityId( int nEntityId )
    {
        _nEntityId = nEntityId;
    }

    /**
     * Returns the id of the revision that was current when this suggestion was
     * created. Used as the diff base in the side-by-side view so the
     * differences stay visible even after the suggestion has been approved.
     *
     * @return the parent revision id, or 0 if not set (legacy rows)
     */
    public int getParentRevisionId( )
    {
        return _nParentRevisionId;
    }

    /**
     * Sets the parent revision id.
     *
     * @param nParentRevisionId
     *            the parent revision id
     */
    public void setParentRevisionId( int nParentRevisionId )
    {
        _nParentRevisionId = nParentRevisionId;
    }

    /**
     * Returns the proposed title.
     *
     * @return the title
     */
    public String getTitle( )
    {
        return _strTitle;
    }

    /**
     * Sets the proposed title.
     *
     * @param strTitle
     *            the title
     */
    public void setTitle( String strTitle )
    {
        _strTitle = strTitle;
    }

    /**
     * Returns the proposed description.
     *
     * @return the description
     */
    public String getDescription( )
    {
        return _strDescription;
    }

    /**
     * Sets the proposed description.
     *
     * @param strDescription
     *            the description
     */
    public void setDescription( String strDescription )
    {
        _strDescription = strDescription;
    }

    /**
     * Returns the proposed content.
     *
     * @return the content
     */
    public String getContent( )
    {
        return _strContent;
    }

    /**
     * Sets the proposed content.
     *
     * @param strContent
     *            the content
     */
    public void setContent( String strContent )
    {
        _strContent = strContent;
    }

    /**
     * Returns the comment provided by the proposer along with the suggestion.
     *
     * @return the comment
     */
    public String getComment( )
    {
        return _strComment;
    }

    /**
     * Sets the comment provided by the proposer.
     *
     * @param strComment
     *            the comment
     */
    public void setComment( String strComment )
    {
        _strComment = strComment;
    }

    /**
     * Returns the display name of the proposer.
     *
     * @return the author display name
     */
    public String getAuthor( )
    {
        return _strAuthor;
    }

    /**
     * Sets the display name of the proposer.
     *
     * @param strAuthor
     *            the author display name
     */
    public void setAuthor( String strAuthor )
    {
        _strAuthor = strAuthor;
    }

    /**
     * Returns the proposer's user guid (lutece user identifier).
     *
     * @return the author guid
     */
    public String getAuthorGuid( )
    {
        return _strAuthorGuid;
    }

    /**
     * Sets the proposer's user guid.
     *
     * @param strAuthorGuid
     *            the author guid
     */
    public void setAuthorGuid( String strAuthorGuid )
    {
        _strAuthorGuid = strAuthorGuid;
    }

    /**
     * Returns the current status of the suggestion.
     *
     * @return the status (PENDING/APPROVED/REJECTED)
     */
    public SuggestionStatus getStatus( )
    {
        return _status;
    }

    /**
     * Sets the current status of the suggestion.
     *
     * @param status
     *            the status
     */
    public void setStatus( SuggestionStatus status )
    {
        _status = status;
    }

    /**
     * Returns the comment provided by the reviewer at approval/rejection time.
     *
     * @return the review comment
     */
    public String getReviewComment( )
    {
        return _strReviewComment;
    }

    /**
     * Sets the comment provided by the reviewer.
     *
     * @param strReviewComment
     *            the review comment
     */
    public void setReviewComment( String strReviewComment )
    {
        _strReviewComment = strReviewComment;
    }

    /**
     * Returns the display name of the reviewer.
     *
     * @return the reviewer display name
     */
    public String getReviewer( )
    {
        return _strReviewer;
    }

    /**
     * Sets the display name of the reviewer.
     *
     * @param strReviewer
     *            the reviewer display name
     */
    public void setReviewer( String strReviewer )
    {
        _strReviewer = strReviewer;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return the creation date
     */
    public Timestamp getDateCreation( )
    {
        return _dateCreation;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param dateCreation
     *            the creation date
     */
    public void setDateCreation( Timestamp dateCreation )
    {
        _dateCreation = dateCreation;
    }

    /**
     * Returns the review timestamp.
     *
     * @return the review date
     */
    public Timestamp getDateReview( )
    {
        return _dateReview;
    }

    /**
     * Sets the review timestamp.
     *
     * @param dateReview
     *            the review date
     */
    public void setDateReview( Timestamp dateReview )
    {
        _dateReview = dateReview;
    }
}
