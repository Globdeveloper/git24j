package com.github.git24j.core;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class Note extends CAutoReleasable {
    protected Note(boolean isWeak, long rawPtr) {
        super(isWeak, rawPtr);
    }

    @Override
    protected void freeOnce(long cPtr) {
        jniFree(cPtr);
    }
    // no matching type found for 'git_note_foreach_cb note_cb'
    /**
     * int git_note_foreach(git_repository *repo, const char *notes_ref, git_note_foreach_cb
     * note_cb, void *payload);
     */
    @FunctionalInterface
    interface ForeachCb {
        int accept(Oid blobId, Oid annotatedObjectId);
    }

    public static class Iterator extends CAutoReleasable {
        protected Iterator(boolean isWeak, long rawPtr) {
            super(isWeak, rawPtr);
        }

        @Override
        protected void freeOnce(long cPtr) {
            jniIteratorFree(cPtr);
        }
    }

    public static class Entry {
        private final Oid oid;
        private final Oid annotatedId;

        public Entry(Oid oid, Oid annotatedId) {
            this.oid = oid;
            this.annotatedId = annotatedId;
        }
    }
    /** -------- Jni Signature ---------- */
    /**
     * int git_note_iterator_new(git_note_iterator **out, git_repository *repo, const char
     * *notes_ref);
     */
    static native int jniIteratorNew(AtomicLong out, long repoPtr, String notesRef);

    /**
     * Creates a new iterator for notes
     *
     * <p>The iterator must be freed manually by the user.
     *
     * @param repo repository where to look up the note
     * @param notesRef canonical name of the reference to use (optional); defaults to
     *     "refs/notes/commits"
     * @return iterator
     * @throws GitException git errors
     */
    @Nonnull
    public Iterator iteratorNew(@Nonnull Repository repo, @Nullable String notesRef) {
        Iterator out = new Iterator(false, 0);
        Error.throwIfNeeded(jniIteratorNew(out._rawPtr, repo.getRawPointer(), notesRef));
        return out;
    }

    /** int git_note_commit_iterator_new(git_note_iterator **out, git_commit *notes_commit); */
    static native int jniCommitIteratorNew(AtomicLong out, long notesCommit);

    /**
     * Creates a new iterator for notes from a commit
     *
     * <p>The iterator must be freed manually by the user.
     *
     * @param notesCommit a pointer to the notes commit object
     * @return iterator
     * @throws GitException git errors
     */
    @Nonnull
    public Iterator commitIteratorNew(@Nonnull Commit notesCommit) {
        Iterator out = new Iterator(false, 0);
        Error.throwIfNeeded(jniCommitIteratorNew(out._rawPtr, notesCommit.getRawPointer()));
        return out;
    }

    /** void git_note_iterator_free(git_note_iterator *it); */
    static native void jniIteratorFree(long it);

    /** int git_note_next(git_oid *note_id, git_oid *annotated_id, git_note_iterator *it); */
    static native int jniNext(Oid note_id, Oid annotated_id, long it);

    /**
     * int git_note_read(git_note **out, git_repository *repo, const char *notes_ref, const git_oid
     * *oid);
     */
    static native int jniRead(AtomicLong out, long repoPtr, String notesRef, Oid oid);
    /**
     * Read the note for an object
     *
     * <p>The note must be freed manually by the user.
     *
     * @param repo repository where to look up the note
     * @param notesRef canonical name of the reference to use (optional); defaults to
     *     "refs/notes/commits"
     * @param oid OID of the git object to read the note from
     * @return read note or null if none available
     * @throws GitException git errors
     */
    @CheckForNull
    public static Note read(@Nonnull Repository repo, @Nonnull String notesRef, @Nonnull Oid oid) {
        Note out = new Note(false, 0);
        Error.throwIfNeeded(jniRead(out._rawPtr, repo.getRawPointer(), notesRef, oid));
        if (out._rawPtr.get() == 0) {
            return null;
        }
        return out;
    }

    /**
     * int git_note_commit_read(git_note **out, git_repository *repo, git_commit *notes_commit,
     * const git_oid *oid);
     */
    static native int jniCommitRead(AtomicLong out, long repoPtr, long notesCommit, Oid oid);
    /**
     * Read the note for an object from a note commit
     *
     * <p>The note must be freed manually by the user.
     *
     * @param repo repository where to look up the note
     * @param notesCommit a pointer to the notes commit object
     * @param oid OID of the git object to read the note from
     * @return read note or null if none available
     * @throws GitException git errors
     */
    @CheckForNull
    public static Note commitRead(
            @Nonnull Repository repo, @Nonnull Commit notesCommit, @Nonnull Oid oid) {
        Note note = new Note(false, 0);
        Error.throwIfNeeded(
                jniCommitRead(
                        note._rawPtr, repo.getRawPointer(), notesCommit.getRawPointer(), oid));
        if (note._rawPtr.get() == 0) {
            return null;
        }
        return note;
    }

    /** const git_signature * git_note_author(const git_note *note); */
    static native long jniAuthor(long note);

    /** @return the note author */
    @CheckForNull
    public Signature author() {
        long ptr = jniAuthor(getRawPointer());
        if (ptr == 0) {
            return null;
        }
        return new Signature(false, ptr);
    }

    /** const git_signature * git_note_committer(const git_note *note); */
    static native long jniCommitter(long note);

    /** @return the note committer */
    public Signature committer() {
        long ptr = jniCommitter(getRawPointer());
        return ptr == 0 ? null : new Signature(false, ptr);
    }

    /** const char * git_note_message(const git_note *note); */
    static native String jniMessage(long note);

    /** @return the note message */
    @Nonnull
    public String message() {
        String msg = jniMessage(getRawPointer());
        return msg == null ? "" : msg;
    }

    /** const git_oid * git_note_id(const git_note *note); */
    static native byte[] jniId(long note);

    /** @return note commit id */
    @CheckForNull
    public Oid id() {
        byte[] raw = jniId(getRawPointer());
        return raw == null ? null : Oid.of(raw);
    }

    /**
     * int git_note_create(git_oid *out, git_repository *repo, const char *notes_ref, const
     * git_signature *author, const git_signature *committer, const git_oid *oid, const char *note,
     * int force);
     */
    static native int jniCreate(
            Oid out,
            long repoPtr,
            String notesRef,
            long author,
            long committer,
            Oid oid,
            String note,
            int force);

    /**
     * Add a note for an object
     *
     * @param repo repository where to store the note
     * @param notesRef canonical name of the reference to use (optional); defaults to
     *     "refs/notes/commits"
     * @param author signature of the notes commit author
     * @param committer signature of the notes commit committer
     * @param oid OID of the git object to decorate
     * @param note Content of the note to add for object oid
     * @param force Overwrite existing note
     * @return oid to the the crated note or empty in case of error
     * @throws GitException git errors
     */
    @Nonnull
    public static Optional<Oid> create(
            @Nonnull Repository repo,
            @Nullable String notesRef,
            @Nonnull Signature author,
            @Nonnull Signature committer,
            @Nonnull Oid oid,
            @Nonnull String note,
            boolean force) {
        Oid outOid = new Oid();
        Error.throwIfNeeded(
                jniCreate(
                        outOid,
                        repo.getRawPointer(),
                        notesRef,
                        author.getRawPointer(),
                        committer.getRawPointer(),
                        oid,
                        note,
                        force ? 1 : 0));
        return outOid.getId() == null ? Optional.empty() : Optional.of(outOid);
    }

    /**
     * int git_note_commit_create(git_oid *notes_commit_out, git_oid *notes_blob_out, git_repository
     * *repo, git_commit *parent, const git_signature *author, const git_signature *committer, const
     * git_oid *oid, const char *note, int allow_note_overwrite);
     */
    static native int jniCommitCreate(
            Oid notes_commit_out,
            Oid notes_blob_out,
            long repoPtr,
            long parent,
            long author,
            long committer,
            Oid oid,
            String note,
            int allowNoteOverwrite);

    /** result of adding note for an object from a commit */
    public static class CommitCreateResult {
        private final Oid _commit;
        private final Oid _blob;

        public CommitCreateResult(@Nullable Oid commit, @Nullable Oid blob) {
            _commit = commit;
            _blob = blob;
        }

        public Oid getCommit() {
            return _commit;
        }

        public Oid getBlob() {
            return _blob;
        }
    }
    /**
     * Add a note for an object from a commit
     *
     * <p>This function will create a notes commit for a given object, the commit is a dangling
     * commit, no reference is created.
     *
     * @param repo repository where the note will live
     * @param parent Pointer to parent note or NULL if this shall start a new notes tree
     * @param author signature of the notes commit author
     * @param committer signature of the notes commit committer
     * @param oid OID of the git object to decorate
     * @param note Content of the note to add for object oid
     * @param allowNoteOverwrite Overwrite existing note
     * @return result object that holds id of the note and the id of the note blob
     * @throws GitException git errors
     */
    @Nonnull
    public static CommitCreateResult commitCreate(
            @Nonnull Repository repo,
            @Nullable Commit parent,
            @Nonnull Signature author,
            @Nonnull Signature committer,
            @Nonnull Oid oid,
            @Nonnull String note,
            boolean allowNoteOverwrite) {
        Oid notesCommitOut = new Oid();
        Oid notesBlobOut = new Oid();
        Error.throwIfNeeded(
                jniCommitCreate(
                        notesCommitOut,
                        notesBlobOut,
                        repo.getRawPointer(),
                        parent == null ? 0 : parent.getRawPointer(),
                        author.getRawPointer(),
                        committer.getRawPointer(),
                        oid,
                        note,
                        allowNoteOverwrite ? 1 : 0));
        return new CommitCreateResult(notesCommitOut, notesBlobOut);
    }

    /**
     * int git_note_remove(git_repository *repo, const char *notes_ref, const git_signature *author,
     * const git_signature *committer, const git_oid *oid);
     */
    static native int jniRemove(
            long repoPtr, String notesRef, long author, long committer, Oid oid);

    /**
     * Remove the note for an object
     *
     * @param repo repository where the note lives
     * @param notesRef canonical name of the reference to use (optional); defaults to
     *     "refs/notes/commits"
     * @param author signature of the notes commit author
     * @param committer signature of the notes commit committer
     * @param oid OID of the git object to remove the note from
     * @throws GitException git errors
     */
    public static void remove(
            @Nonnull Repository repo,
            @Nullable String notesRef,
            @Nonnull Signature author,
            @Nonnull Signature committer,
            @Nonnull Oid oid) {
        Error.throwIfNeeded(
                jniRemove(
                        repo.getRawPointer(),
                        notesRef,
                        author.getRawPointer(),
                        committer.getRawPointer(),
                        oid));
    }

    /**
     * int git_note_commit_remove(git_oid *notes_commit_out, git_repository *repo, git_commit
     * *notes_commit, const git_signature *author, const git_signature *committer, const git_oid
     * *oid);
     */
    static native int jniCommitRemove(
            Oid notes_commit_out,
            long repoPtr,
            long notesCommit,
            long author,
            long committer,
            Oid oid);

    /**
     * Remove the note for an object When removing a note a new tree containing all notes sans the
     * note to be removed is created and a new commit pointing to that tree is also created. In the
     * case where the resulting tree is an empty tree a new commit pointing to this empty tree will
     * be returned.
     *
     * @param repo repository where the note lives
     * @param notesCommit a pointer to the notes commit object
     * @param author signature of the notes commit author
     * @param committer signature of the notes commit committer
     * @param oid OID of the git object to remove the note from
     * @return the new notes commit or empty in case of error.
     * @throws GitException git errors
     */
    public static Optional<Oid> commitRemove(
            @Nonnull Repository repo,
            @Nonnull Commit notesCommit,
            @Nonnull Signature author,
            @Nonnull Signature committer,
            @Nonnull Oid oid) {
        Oid out = new Oid();
        Error.throwIfNeeded(
                jniCommitRemove(
                        out,
                        repo.getRawPointer(),
                        notesCommit.getRawPointer(),
                        author.getRawPointer(),
                        committer.getRawPointer(),
                        oid));
        return out.getId() == null ? Optional.empty() : Optional.of(out);
    }

    /** void git_note_free(git_note *note); */
    static native void jniFree(long note);

    /** int git_note_default_ref(git_buf *out, git_repository *repo); */
    static native int jniDefaultRef(Buf out, long repoPtr);

    /**
     * Get the default notes reference for a repository
     *
     * @param repo The Git repository
     * @return the name of the default notes reference
     * @throws GitException git errors
     */
    public static Optional<String> defaultRef(Repository repo) {
        Buf out = new Buf();
        Error.throwIfNeeded(jniDefaultRef(out, repo.getRawPointer()));
        return out.getString();
    }

    /**
     * int git_note_foreach(git_repository *repo, const char *notes_ref, git_note_foreach_cb
     * note_cb, void *payload);
     */
    static native int jniForeach(
            long repoPtr, String notesRef, Internals.BArrBarrCallback bbCallback);

    public void foreach(
            @Nonnull Repository repo, @Nullable String notesRef, @Nonnull ForeachCb cb) {
        Error.throwIfNeeded(
                jniForeach(
                        repo.getRawPointer(),
                        notesRef,
                        (id1, id2) -> cb.accept(Oid.of(id1), Oid.of(id2))));
    }
}
